/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.jitexecutor.efesto.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.util.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.pmml.PMML4Result;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.kogito.jitexecutor.efesto.dmn.model.JITDMNDecisionResult;
import org.kie.kogito.jitexecutor.efesto.dmn.model.JITDMNResult;
import org.kie.kogito.jitexecutor.efesto.requests.JITEfestoPayload;
import org.kie.kogito.jitexecutor.efesto.requests.MultipleResourcesPayload;
import org.kie.kogito.jitexecutor.efesto.requests.ResourceWithURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.DMN_FILE;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.DMN_PMML_FILE;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.PMML_FILE;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.PMML_MODEL_NAME;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.getResourceWithURI;

@QuarkusTest
class JITEfestoResourceTest {

    private static final Logger LOG = LoggerFactory.getLogger(JITEfestoResourceTest.class);

    private static String pmmlModel;
    private static String dmnModel;
    private static String dmnPmmlModel;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final CollectionType LIST_OF_MSGS = MAPPER.getTypeFactory()
            .constructCollectionType(List.class,
                    String.class);

    @BeforeAll
    public static void setup() throws IOException {
        pmmlModel = FileUtils.getFileContent(PMML_FILE);
        dmnModel = FileUtils.getFileContent(DMN_FILE);
        dmnPmmlModel = FileUtils.getFileContent(DMN_PMML_FILE);
    }

    @Test
    void evaluate_PMML() throws IOException {
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIPMML.getURI(), Collections.singletonList(resourceWithURIPMML));
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 3.0);
        inputData.put("fld2", 2.0);
        inputData.put("fld3", "y");
        JITEfestoPayload payload = new JITEfestoPayload(multipleResourcesPayload, inputData);
        String response = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/jitefesto/evaluate")
                .then()
                .statusCode(200)
                .body(containsString("{\"/pmml/test_regression.pmml\":{\"correlationId\":null," +
                        "\"segmentationId\":null,\"segmentId\":null,\"segmentIndex\":0," +
                        "\"resultCode\":\"OK\",\"resultObjectName\":\"fld4\"," +
                        "\"resultVariables\":{\"result\":52.5,\"fld4\":52.5}}}"))
                .extract()
                .asString();

        LOG.info("Evaluate response: {}", response);
        Map<String, Object> result = MAPPER.readValue(response, Map.class);
        assertThat(result).hasSize(1).containsKey(resourceWithURIPMML.getURI().fullPath());
        Object retrieved = result.get(resourceWithURIPMML.getURI().fullPath());
        PMML4Result pmml4Result = MAPPER.convertValue(retrieved, PMML4Result.class);
        assertThat(pmml4Result.getResultCode()).isEqualTo("OK");
    }

    @Test
    void evaluate_DMN() throws IOException {
        ResourceWithURI resourceWithURI = getResourceWithURI("/dmn/" + DMN_FILE, "", dmnModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURI.getURI(), Collections.singletonList(resourceWithURI));
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("FICO Score", 800);
        inputData.put("DTI Ratio", .1);
        inputData.put("PITI Ratio", .1);
        JITEfestoPayload payload = new JITEfestoPayload(multipleResourcesPayload, inputData);
        String response = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/jitefesto/evaluate")
                .then()
                .statusCode(200)
                .body(containsString("{\"/dmn/test.dmn\":{\"messages\":[]," +
                        "\"decisionResults\":[{\"decisionId\":\"d_Loan_32Approval\",\"decisionName\":\"Loan Approval\",\"result\":\"Approved\",\"messages\":[],\"evaluationStatus\":\"SUCCEEDED\"},{\"decisionId\":\"d_DTI_32Rating\",\"decisionName\":\"DTI Rating\",\"result\":\"Good\",\"messages\":[],\"evaluationStatus\":\"SUCCEEDED\"},{\"decisionId\":\"d_PITI_32Rating\",\"decisionName\":\"PITI Rating\",\"result\":\"Good\",\"messages\":[],\"evaluationStatus\":\"SUCCEEDED\"}]}}"))
                .extract()
                .asString();

        LOG.info("Evaluate response: {}", response);
        Map<String, Object> result = MAPPER.readValue(response, Map.class);
        assertThat(result).hasSize(1).containsKey(resourceWithURI.getURI().fullPath());
        Object retrieved = result.get(resourceWithURI.getURI().fullPath());
        JITDMNResult jitdmnResult = MAPPER.convertValue(retrieved, JITDMNResult.class);
    }

    @Test
    void evaluate_DMNPMML() throws IOException {
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        ResourceWithURI resourceWithURIDMNPMML = getResourceWithURI("/dmn/" + DMN_PMML_FILE, "", dmnPmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIDMNPMML.getURI(), Arrays.asList(resourceWithURIDMNPMML, resourceWithURIPMML));
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 3.0);
        inputData.put("fld2", 2.0);
        inputData.put("fld3", "y");
        JITEfestoPayload payload = new JITEfestoPayload(multipleResourcesPayload, inputData);
        String response = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/jitefesto/evaluate")
                .then()
                .statusCode(200)
                .body(containsString("{\"/dmn/KiePMMLRegression.dmn\":{\"messages\":[]," +
                        "\"decisionResults\":[{\"decisionId\":\"_97845D38-0E4C-41D0-9998" +
                        "-0D6B149751F3\",\"decisionName\":\"Decision\",\"result\":52.5," +
                        "\"messages\":[],\"evaluationStatus\":\"SUCCEEDED\"}]}}"))
                .extract()
                .asString();

        LOG.info("Evaluate response: {}", response);
        Map<String, Object> result = MAPPER.readValue(response, Map.class);
        assertThat(result).hasSize(1).containsKey(resourceWithURIDMNPMML.getURI().fullPath());
        Object retrieved = result.get(resourceWithURIDMNPMML.getURI().fullPath());
        JITDMNResult jitdmnResult = MAPPER.convertValue(retrieved, JITDMNResult.class);
        assertThat(jitdmnResult.getMessages()).isEmpty();
        assertThat(jitdmnResult.getDecisionResults()).hasSize(1);
        JITDMNDecisionResult decisionResult = jitdmnResult.getDecisionResults().get(0);
        assertThat(decisionResult.getEvaluationStatus()).isEqualTo(DMNDecisionResult.DecisionEvaluationStatus.SUCCEEDED);
        assertThat(decisionResult.getResult()).isEqualTo(52.5);
    }

}
