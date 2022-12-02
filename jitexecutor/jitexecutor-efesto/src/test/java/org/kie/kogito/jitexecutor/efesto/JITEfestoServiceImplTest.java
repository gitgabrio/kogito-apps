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
package org.kie.kogito.jitexecutor.efesto;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.util.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.efesto.common.api.identifiers.NamedLocalUriId;
import org.kie.kogito.jitexecutor.efesto.dmn.model.EfestoOutputDMN;
import org.kie.kogito.jitexecutor.efesto.dmn.model.JITDMNDecisionResult;
import org.kie.kogito.jitexecutor.efesto.dmn.model.JITDMNResult;
import org.kie.kogito.jitexecutor.efesto.requests.JitExecutorUri;
import org.kie.kogito.jitexecutor.efesto.requests.MultipleResourcesPayload;
import org.kie.kogito.jitexecutor.efesto.requests.ResourceWithURI;
import org.kie.kogito.jitexecutor.efesto.responses.JITEfestoResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.DMN_FILE;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.DMN_PMML_FILE;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.PMML_FILE;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.PMML_MODEL_NAME;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.getResourceWithURI;

class JITEfestoServiceImplTest {

    private static JITEfestoServiceImpl jitEfestoService;
    private static String pmmlModel;
    private static String dmnModel;
    private static String dmnPmmlModel;

    @BeforeAll
    public static void setup() throws IOException {
        jitEfestoService = new JITEfestoServiceImpl();
        pmmlModel = FileUtils.getFileContent(PMML_FILE);
        dmnModel = FileUtils.getFileContent(DMN_FILE);
        dmnPmmlModel = FileUtils.getFileContent(DMN_PMML_FILE);
    }

    @Test
    void evaluateModel_PMML() {
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIPMML.getURI(), Collections.singletonList(resourceWithURIPMML));
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 3.0);
        inputData.put("fld2", 2.0);
        inputData.put("fld3", "y");
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(multipleResourcesPayload, inputData);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void evaluateModel_DMN() {
        ResourceWithURI resourceWithURI = getResourceWithURI("/dmn/" + DMN_FILE, "", dmnModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURI.getURI(), Collections.singletonList(resourceWithURI));
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("FICO Score", 800);
        inputData.put("DTI Ratio", .1);
        inputData.put("PITI Ratio", .1);
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(multipleResourcesPayload, inputData);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void evaluateModel_DMNPMML() {
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        ResourceWithURI resourceWithURIDMNPMML = getResourceWithURI("/dmn/" + DMN_PMML_FILE, "", dmnPmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIDMNPMML.getURI(), Arrays.asList(resourceWithURIDMNPMML, resourceWithURIPMML));
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 3.0);
        inputData.put("fld2", 2.0);
        inputData.put("fld3", "y");
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(multipleResourcesPayload, inputData);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getEfestoResult()).isInstanceOf(EfestoOutputDMN.class);
        EfestoOutputDMN efestoOutputDMN = (EfestoOutputDMN) retrieved.getEfestoResult();
        assertThat(efestoOutputDMN.getOutputData()).isNotNull();
        JITDMNResult jitdmnResult = efestoOutputDMN.getOutputData();
        assertThat(jitdmnResult.getMessages()).isEmpty();
        assertThat(jitdmnResult.getDecisionResults()).hasSize(1);
        JITDMNDecisionResult decisionResult = jitdmnResult.getDecisionResults().get(0);
        assertThat(decisionResult.getEvaluationStatus()).isEqualTo(DMNDecisionResult.DecisionEvaluationStatus.SUCCEEDED);
        assertThat(decisionResult.getResult()).isEqualTo(BigDecimal.valueOf(52.5));
    }

    @Test
    void evaluateModelCompiled_PMML() {
        // setup
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIPMML.getURI(), Collections.singletonList(resourceWithURIPMML));
        Map<JitExecutorUri, NamedLocalUriId> mappedUris = jitEfestoService.compileModels(multipleResourcesPayload);
        // test
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 3.0);
        inputData.put("fld2", 2.0);
        inputData.put("fld3", "y");
        NamedLocalUriId localUriId = mappedUris.get(resourceWithURIPMML.getURI());
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(localUriId, inputData);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void evaluateModelCompiled_DMN() {
        ResourceWithURI resourceWithURI = getResourceWithURI("/dmn/" + DMN_FILE, "", dmnModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURI.getURI(), Collections.singletonList(resourceWithURI));
        Map<JitExecutorUri, NamedLocalUriId> mappedUris = jitEfestoService.compileModels(multipleResourcesPayload);
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("FICO Score", 800);
        inputData.put("DTI Ratio", .1);
        inputData.put("PITI Ratio", .1);
        NamedLocalUriId localUriId = mappedUris.get(resourceWithURI.getURI());
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(localUriId, inputData);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void evaluateModelCompiled_DMNPMML() {
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        ResourceWithURI resourceWithURIDMNPMML = getResourceWithURI("/dmn/" + DMN_PMML_FILE, "", dmnPmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIDMNPMML.getURI(), Arrays.asList(resourceWithURIDMNPMML, resourceWithURIPMML));
        Map<JitExecutorUri, NamedLocalUriId> mappedUris = jitEfestoService.compileModels(multipleResourcesPayload);
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 3.0);
        inputData.put("fld2", 2.0);
        inputData.put("fld3", "y");
        NamedLocalUriId localUriId = mappedUris.get(resourceWithURIDMNPMML.getURI());
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(localUriId, inputData);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void compileModels() {
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        ResourceWithURI resourceWithURIDMNPMML = getResourceWithURI("/dmn/" + DMN_PMML_FILE, "", dmnPmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIDMNPMML.getURI(), Arrays.asList(resourceWithURIDMNPMML, resourceWithURIPMML));
        Map<JitExecutorUri, NamedLocalUriId> retrieved = jitEfestoService.compileModels(multipleResourcesPayload);
        assertThat(retrieved).hasSameSizeAs(multipleResourcesPayload.getResources())
                .containsKey(resourceWithURIPMML.getURI())
                .containsKey(resourceWithURIDMNPMML.getURI());
    }

    @Test
    void compileModel_PMML() {
        ResourceWithURI resourceWithURI = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        NamedLocalUriId retrieved = jitEfestoService.compileModel(resourceWithURI);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void compileModel_DMN() {
        ResourceWithURI resourceWithURI = getResourceWithURI("/dmn/" + DMN_FILE, "", dmnModel);
        NamedLocalUriId retrieved = jitEfestoService.compileModel(resourceWithURI);
        assertThat(retrieved).isNotNull();
    }

}