package org.kie.kogito.jitexecutor.efesto;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.util.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.efesto.common.api.identifiers.NamedLocalUriId;
import org.kie.kogito.jitexecutor.efesto.requests.MultipleResourcesPayload;
import org.kie.kogito.jitexecutor.efesto.requests.ResourceWithURI;
import org.kie.kogito.jitexecutor.efesto.responses.JITEfestoResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.jitexecutor.efesto.TestingUtils.getResourceWithURI;

class JITEfestoServiceImplTest {

    private static final String PMML_FILE = "test_regression.pmml";

    private static final String PMML_MODEL_NAME = "LinReg";
    private static final String DMN_FILE = "test.dmn";
    private static final String DMN_PMML_FILE = "KiePMMLRegression.dmn";
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
        // setup
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIPMML.getURI(), Collections.singletonList(resourceWithURIPMML));
        // test
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
    }

    @Test
    void evaluateModelCompiled_PMML() {
        // setup
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIPMML.getURI(), Collections.singletonList(resourceWithURIPMML));
        Map<ResourceWithURI, NamedLocalUriId> mappedUris = jitEfestoService.compileModels(multipleResourcesPayload);
        // test
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 3.0);
        inputData.put("fld2", 2.0);
        inputData.put("fld3", "y");
        NamedLocalUriId localUriId = mappedUris.get(resourceWithURIPMML);
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(localUriId, inputData);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void evaluateModelCompiled_DMN() {
        ResourceWithURI resourceWithURI = getResourceWithURI("/dmn/" + DMN_FILE, "", dmnModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURI.getURI(), Collections.singletonList(resourceWithURI));
        Map<ResourceWithURI, NamedLocalUriId> mappedUris = jitEfestoService.compileModels(multipleResourcesPayload);
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("FICO Score", 800);
        inputData.put("DTI Ratio", .1);
        inputData.put("PITI Ratio", .1);
        NamedLocalUriId localUriId = mappedUris.get(resourceWithURI);
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(localUriId, inputData);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void evaluateModelCompiled_DMNPMML() {
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        ResourceWithURI resourceWithURIDMNPMML = getResourceWithURI("/dmn/" + DMN_PMML_FILE, "", dmnPmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIDMNPMML.getURI(), Arrays.asList(resourceWithURIDMNPMML, resourceWithURIPMML));
        Map<ResourceWithURI, NamedLocalUriId> mappedUris = jitEfestoService.compileModels(multipleResourcesPayload);
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("fld1", 3.0);
        inputData.put("fld2", 2.0);
        inputData.put("fld3", "y");
        NamedLocalUriId localUriId = mappedUris.get(resourceWithURIDMNPMML);
        JITEfestoResult retrieved = jitEfestoService.evaluateModel(localUriId, inputData);
        assertThat(retrieved).isNotNull();
    }

    @Test
    void compileModels() {
        ResourceWithURI resourceWithURIPMML = getResourceWithURI("/pmml/" + PMML_FILE, PMML_MODEL_NAME, pmmlModel);
        ResourceWithURI resourceWithURIDMNPMML = getResourceWithURI("/dmn/" + DMN_PMML_FILE, "", dmnPmmlModel);
        MultipleResourcesPayload multipleResourcesPayload =
                new MultipleResourcesPayload(resourceWithURIDMNPMML.getURI(), Arrays.asList(resourceWithURIDMNPMML, resourceWithURIPMML));
        Map<ResourceWithURI, NamedLocalUriId>  retrieved = jitEfestoService.compileModels(multipleResourcesPayload);
        assertThat(retrieved).hasSameSizeAs(multipleResourcesPayload.getResources())
                .containsKey(resourceWithURIPMML)
                .containsKey(resourceWithURIDMNPMML);
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