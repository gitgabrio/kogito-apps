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
package org.kie.kogito.jitexecutor.bpmn;

import org.drools.io.FileSystemResource;
import org.drools.util.IoUtils;
import org.drools.util.StringUtils;
import org.jbpm.process.core.impl.ProcessImpl;
import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.process.core.validation.impl.ProcessValidationErrorImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.kogito.jitexecutor.bpmn.responses.JITBPMNValidationResult;
import org.kie.kogito.jitexecutor.bpmn.responses.MultipleResponsesPayload;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kie.kogito.jitexecutor.bpmn.TestingUtils.*;

class JITBPMNServiceImplTest {

    private static final JITBPMNService jitBpmnService = new JITBPMNServiceImpl();

    @Test
    void validateModel_SingleValidBPMN2() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_BPMN2_FILE))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().isEmpty();
    }

    @Test
    void validateModel_MultipleValidBPMN2() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(MULTIPLE_BPMN2_FILE))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().isEmpty();
    }

    @Test
    void validateModel_SingleInvalidBPMN2() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_INVALID_BPMN2_FILE))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(2);
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getProcessId().equals("invalid") &&
                result.getErrorMessage().equals("Process has no start node."));
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getProcessId().equals("invalid") &&
                result.getErrorMessage().equals("Process has no end node."));
    }

    @Test
    void validateModel_MultipleInvalidBPMN2() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(MULTIPLE_INVALID_BPMN2_FILE))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(4);
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getProcessId().equals("invalid1") &&
                result.getErrorMessage().equals("Process has no start node."));
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getProcessId().equals("invalid1") &&
                result.getErrorMessage().equals("Process has no end node."));
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getProcessId().equals("invalid2") &&
                result.getErrorMessage().equals("Process has no start node."));
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getProcessId().equals("invalid2") &&
                result.getErrorMessage().equals("Process has no end node."));
    }

    @Test
    void validateModel_CatchNowhereBPMN() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(CATCH_NOWHERE))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(1);
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.SEVERE) &&
                result.getNodeId() == 1 &&
                result.getNodeName().equals("(unknown)") &&
                result.getProcessId().equals("catch_nowhere") &&
                result.getErrorMessage().equals("Event node 'null' [1] has no incoming connection"));
    }

    @Test
    void validateModel_AdHocSubprocessBPMN() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(AD_HOC_SUBPROCESS))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(5);
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getNodeId() == 0 &&
                result.getNodeName() == null &&
                result.getProcessId().equals("ad_hoc_subprocess") &&
                result.getErrorMessage().equals("Process has no start node."));
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getNodeId() == 0 &&
                result.getNodeName() == null &&
                result.getProcessId().equals("ad_hoc_subprocess") &&
                result.getErrorMessage().equals("Process has no end node."));
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getNodeId() == 1 &&
                result.getNodeName().equals("Sub-process") &&
                result.getProcessId().equals("ad_hoc_subprocess") &&
                result.getErrorMessage().equals("Node 'Sub-process' [1] Dynamic has no incoming connection"));
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getNodeId() == 1 &&
                result.getNodeName().equals("Sub-process") &&
                result.getProcessId().equals("ad_hoc_subprocess") &&
                result.getErrorMessage().equals("Node 'Sub-process' [1] Dynamic has no outgoing connection"));
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.WARNING) &&
                result.getNodeId() == 1 &&
                result.getNodeName().equals("Sub-process") &&
                result.getProcessId().equals("ad_hoc_subprocess") &&
                result.getErrorMessage().equals("Node 'Sub-process' [1] Has no connection to the start node."));
    }

    @Test
    void validateModel_LaneWithAnnotationBPMN() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(LANE_WITH_ANNOTATION))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(1);
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.SEVERE) &&
                result.getNodeId() == -1 &&
                result.getNodeName().equals("") &&
                result.getProcessId().equals("") &&
                result.getErrorMessage().equals("Could not find source [_72784A45-EB33-4E6B-B4D6-1E651282BF7C] for association _047C7DDC-8D2B-423A-9574-65A847490E71]"));
    }

    @Disabled("condition on edge refer to non existing variable, but no problem reported from validation")
    @Test
    void validateModel_InvalidExpressionBPMN() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(INVALID_EXPRESSION))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(1);
    }

    @Disabled("error event do not specify error. is there some default behavior that all errors are assumed in similar case?")
    @Test
    void validateModel_MissingErrorBPMN() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(MISSING_ERROR))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(1);
    }

    @Disabled("in the task assignments of the business rule task, we assign 'Result' to '[nothing]', maybe valid from spec point of view, but doesn't make much sense I think")
    @Test
    void validateModel_MissingProcessVariableBPMN() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(MISSING_PROCESS_VARIABLE))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(1);
    }

    @Disabled("signal event do not specify signal. is there some default behavior that all signals are assumed in similar case?")
    @Test
    void validateModel_MissingSignalBPMN() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(MISSING_SIGNAL))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(1);
    }

    @Test
    void validateModel_SingleUnparsableBPMN2() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_UNPARSABLE_BPMN2_FILE))));
        MultipleResponsesPayload retrieved = jitBpmnService.validateModel(toValidate);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResults()).isNotNull().hasSize(1);
        assertThat(retrieved.getResults()).anyMatch(result -> result.getErrorLevel().equals(JITBPMNValidationResult.ERROR_LEVEL.SEVERE) &&
                result.getNodeId() == -1 &&
                result.getNodeName().equals("") &&
                result.getProcessId().equals("") &&
                result.getErrorMessage().equals("Could not find message _T6T0kEcTEDuygKsUt0on2Q____"));
    }

    @Test
    void parseModelXml_SingleValidBPMN2() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(SINGLE_BPMN2_FILE))));
        Collection<Process> retrieved = JITBPMNServiceImpl.parseModelXml(toValidate);
        assertThat(retrieved).isNotNull().hasSize(1);
    }

    @Test
    void parseModelXml_MultipleValidBPMN2() throws IOException {
        String toValidate = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(MULTIPLE_BPMN2_FILE))));
        Collection<Process> retrieved = JITBPMNServiceImpl.parseModelXml(toValidate);
        assertThat(retrieved).isNotNull().hasSize(2);
    }

    @Test
    void parseModelXml_UnparsableBPMN2() throws IOException {
        String toParse = new String(IoUtils.readBytesFromInputStream(Objects.requireNonNull(JITBPMNService.class.getResourceAsStream(UNPARSABLE_BPMN2_FILE))));
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> JITBPMNServiceImpl.parseModelXml(toParse),
                "Expected parseModelXml to throw, but it didn't");
        String expectedMessage = "Could not parse";
        assertThat(thrown.getMessage()).contains(expectedMessage);
    }

    @Test
    void parseModelResource_SingleValidBPMN2() {
        Collection<Process> retrieved = JITBPMNServiceImpl.parseModelResource(new FileSystemResource(new File(JITBPMNService.class.getResource(SINGLE_BPMN2_FILE).getFile())));
        assertThat(retrieved).isNotNull().hasSize(1);
    }

    @Test
    void parseModelResource_MultipleValidBPMN2() {
        Collection<Process> retrieved = JITBPMNServiceImpl.parseModelResource(new FileSystemResource(new File(JITBPMNService.class.getResource(MULTIPLE_BPMN2_FILE).getFile())));
        assertThat(retrieved).isNotNull().hasSize(2);
    }

    @Test
    void parseModelResource_UnparsableBPMN2() {
        Resource resource = new FileSystemResource(new File(UNPARSABLE_BPMN2_FILE));
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> JITBPMNServiceImpl.parseModelResource(resource),
                "Expected parseModelXml to throw, but it didn't");
        String expectedMessage = "Could not parse";
        assertThat(thrown.getMessage()).contains(expectedMessage);
    }

    @Test
    void getErrorString() {
        Process process = new ProcessImpl();
        String id = StringUtils.generateUUID();
        String name = StringUtils.generateUUID();
        ((ProcessImpl) process).setId(id);
        ((ProcessImpl) process).setName(name);
        String message = StringUtils.generateUUID();
        ProcessValidationError processValidationError = new ProcessValidationErrorImpl(process, message);
        String expected = "Uri: (unknown) - Process id: " + id + " - name : " + name + " - error : " + message;
        String retrieved = JITBPMNServiceImpl.getErrorString(processValidationError, null);
        assertThat(retrieved).isEqualTo(expected);
        String uri = "uri";
        expected = "Uri: " + uri + " - Process id: " + id + " - name : " + name + " - error : " + message;
        retrieved = JITBPMNServiceImpl.getErrorString(processValidationError, uri);
        assertThat(retrieved).isEqualTo(expected);
    }

}
