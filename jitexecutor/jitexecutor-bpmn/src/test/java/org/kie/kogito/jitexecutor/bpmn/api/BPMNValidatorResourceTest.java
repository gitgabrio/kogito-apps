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

package org.kie.kogito.jitexecutor.bpmn.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.drools.util.IoUtils;
import org.junit.jupiter.api.Test;
import org.kie.kogito.jitexecutor.bpmn.JITBPMNService;
import org.kie.kogito.jitexecutor.bpmn.responses.MultipleResponsesPayload;
import org.kie.kogito.jitexecutor.common.requests.MultipleResourcesPayload;
import org.kie.kogito.jitexecutor.common.requests.ResourceWithURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.kie.kogito.jitexecutor.bpmn.TestingUtils.*;

@QuarkusTest
public class BPMNValidatorResourceTest {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNValidatorResourceTest.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void test_SingleValidBPMN2() throws IOException {
        String toValidate =
                new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_BPMN2_FILE))));
        String uri = "uri";
        String response = given()
                .contentType(ContentType.JSON)
                .body(getMultipleResourcePayload(toValidate, uri))
                .when()
                .post("/jitbpmn/validate")
                .then()
                .statusCode(200)
                .body(containsString("[]"))
                .extract()
                .asString();

        LOG.info("Validate response: {}", response);
        MultipleResponsesPayload responsePayload = MAPPER.readValue(response, MultipleResponsesPayload.class);
        assertThat(responsePayload.getResults()).isEmpty();
    }

    @Test
    void validateModel_MultipleValidBPMN2() throws IOException {
        String toValidate =
                new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(MULTIPLE_BPMN2_FILE))));
        String uri = "uri";
        String response = given()
                .contentType(ContentType.JSON)
                .body(getMultipleResourcePayload(toValidate, uri))
                .when()
                .post("/jitbpmn/validate")
                .then()
                .statusCode(200)
                .body(containsString("[]"))
                .extract()
                .asString();

        LOG.info("Validate response: {}", response);
        MultipleResponsesPayload responsePayload = MAPPER.readValue(response, MultipleResponsesPayload.class);
        assertThat(responsePayload.getResults()).isEmpty();
    }

    @Test
    void validateModel_SingleInvalidBPMN2() throws IOException {
        String toValidate =
                new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_INVALID_BPMN2_FILE))));
        String uri = "uri";
        String response = given()
                .contentType(ContentType.JSON)
                .body(getMultipleResourcePayload(toValidate, uri))
                .when()
                .post("/jitbpmn/validate")
                .then()
                .statusCode(200)
                .body(containsString("{\"mainURI\":\"uri\",\"results\":[{\"errorLevel\":\"WARNING\",\"nodeId\":0,\"nodeName\":null,\"processId\":\"invalid\",\"errorMessage\":\"Process has no start node.\"},{\"errorLevel\":\"WARNING\",\"nodeId\":0,\"nodeName\":null,\"processId\":\"invalid\",\"errorMessage\":\"Process has no end node.\"}]}"))
                .extract()
                .asString();

        LOG.info("Validate response: {}", response);
        MultipleResponsesPayload responsePayload = MAPPER.readValue(response, MultipleResponsesPayload.class);
        assertThat(responsePayload.getResults()).hasSize(2);
    }

    @Test
    void validateModel_MultipleInvalidBPMN2() throws IOException {
        String toValidate =
                new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(MULTIPLE_INVALID_BPMN2_FILE))));
        String uri = "uri";
        String response = given()
                .contentType(ContentType.JSON)
                .body(getMultipleResourcePayload(toValidate, uri))
                .when()
                .post("/jitbpmn/validate")
                .then()
                .statusCode(200)
                .body(containsString("{\"mainURI\":\"uri\",\"results\":[{\"errorLevel\":\"WARNING\",\"nodeId\":0,\"nodeName\":null,\"processId\":\"invalid1\",\"errorMessage\":\"Process has no start node.\"},{\"errorLevel\":\"WARNING\",\"nodeId\":0,\"nodeName\":null,\"processId\":\"invalid1\",\"errorMessage\":\"Process has no end node.\"},{\"errorLevel\":\"WARNING\",\"nodeId\":0,\"nodeName\":null,\"processId\":\"invalid2\",\"errorMessage\":\"Process has no start node.\"},{\"errorLevel\":\"WARNING\",\"nodeId\":0,\"nodeName\":null,\"processId\":\"invalid2\",\"errorMessage\":\"Process has no end node.\"}]}"))
                .extract()
                .asString();

        LOG.info("Validate response: {}", response);
        MultipleResponsesPayload responsePayload = MAPPER.readValue(response, MultipleResponsesPayload.class);
        assertThat(responsePayload.getResults()).hasSize(4);
    }

    @Test
    void validateModel_MultipleBPMN2() throws IOException {
        String validModel =
                new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_BPMN2_FILE))));
        String invalidModel =
                new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_INVALID_BPMN2_FILE))));
        ResourceWithURI validResource = new ResourceWithURI("UriValid", validModel);
        ResourceWithURI invalidResource = new ResourceWithURI("UriInvalid", invalidModel);
        String response = given()
                .contentType(ContentType.JSON)
                .body(new MultipleResourcesPayload("mainUri", Arrays.asList(validResource, invalidResource)))
                .when()
                .post("/jitbpmn/validate")
                .then()
                .statusCode(200)
                .body(containsString("{\"mainURI\":\"mainUri\",\"results\":[{\"errorLevel\":\"WARNING\",\"nodeId\":0,\"nodeName\":null,\"processId\":\"invalid\",\"errorMessage\":\"Process has no start node.\"},{\"errorLevel\":\"WARNING\",\"nodeId\":0,\"nodeName\":null,\"processId\":\"invalid\",\"errorMessage\":\"Process has no end node.\"}]}"))
                .extract()
                .asString();

        LOG.info("Validate response: {}", response);
        MultipleResponsesPayload responsePayload = MAPPER.readValue(response, MultipleResponsesPayload.class);
        assertThat(responsePayload.getResults()).hasSize(2);
    }

    @Test
    void validateModel_SingleUnparsablePMN2() throws IOException {
        String toValidate =
                new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_UNPARSABLE_BPMN2_FILE))));
        String uri = "uri";
        String response = given()
                .contentType(ContentType.JSON)
                .body(getMultipleResourcePayload(toValidate, uri))
                .when()
                .post("/jitbpmn/validate")
                .then()
                .statusCode(200)
                .body(containsString("{\"mainURI\":\"uri\",\"results\":[{\"errorLevel\":\"SEVERE\",\"nodeId\":-1,\"nodeName\":\"\",\"processId\":\"\",\"errorMessage\":\"Could not find message _T6T0kEcTEDuygKsUt0on2Q____\"}]}"))
                .extract()
                .asString();

        LOG.info("Validate response: {}", response);
        MultipleResponsesPayload responsePayload = MAPPER.readValue(response, MultipleResponsesPayload.class);
        assertThat(responsePayload.getResults()).hasSize(1);
    }

    private MultipleResourcesPayload getMultipleResourcePayload(String content, String uri) {
        return new MultipleResourcesPayload(uri, Collections.singletonList(new ResourceWithURI(uri, content)));
    }

}
