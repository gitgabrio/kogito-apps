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

import org.drools.io.InputStreamResource;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.compiler.xml.core.SemanticModules;
import org.jbpm.process.core.validation.ProcessValidationError;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.kogito.jitexecutor.bpmn.overrides.JITBPMNSemanticModule;
import org.kie.kogito.jitexecutor.bpmn.overrides.JITProcessParsingValidationException;
import org.kie.kogito.jitexecutor.bpmn.overrides.JITProcessValidationError;
import org.kie.kogito.jitexecutor.bpmn.overrides.JITRuleFlowProcessValidator;
import org.kie.kogito.jitexecutor.bpmn.responses.JITBPMNValidationResult;
import org.kie.kogito.jitexecutor.bpmn.responses.MultipleResponsesPayload;
import org.kie.kogito.jitexecutor.common.requests.MultipleResourcesPayload;
import org.kie.kogito.jitexecutor.common.requests.ResourceWithURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@ApplicationScoped
public class JITBPMNServiceImpl implements JITBPMNService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JITBPMNServiceImpl.class);

    private static final JITRuleFlowProcessValidator PROCESS_VALIDATOR = JITRuleFlowProcessValidator.getInstance();

    private static final SemanticModules BPMN_SEMANTIC_MODULES = new SemanticModules();

    private static String ERROR_TEMPLATE = "Uri: %s - Process id: %s - name : %s - error : %s";

    static {
        BPMN_SEMANTIC_MODULES.addSemanticModule(new JITBPMNSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNExtensionsSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNDISemanticModule());
    }

    @Override
    public MultipleResponsesPayload validatePayload(MultipleResourcesPayload payload) {
        Collection<JITBPMNValidationResult> errors = new ArrayList<>();
        for (ResourceWithURI resourceWithURI : payload.getResources()) {
            errors.addAll(collectErrors(resourceWithURI.getContent(), resourceWithURI.getURI()));
        }
        return new MultipleResponsesPayload(payload.getMainURI(), errors);
    }

    @Override
    public MultipleResponsesPayload validateModel(String modelXML) {
        LOGGER.trace("Received\n{}", modelXML);
        Collection<JITBPMNValidationResult> errors = collectErrors(modelXML, null);
        return new MultipleResponsesPayload("", errors);
    }

    static Collection<JITBPMNValidationResult> collectErrors(String modelXML, String resourceUri) {
        LOGGER.trace("Received\n{}", modelXML);
        Collection<JITBPMNValidationResult> toReturn;
        Collection<Process> processes;
        try {
            processes = parseModelXml(modelXML);
            if (processes.isEmpty()) {
                String error = "No process found";
                if (resourceUri != null) {
                    error += " on resource " + resourceUri;
                }
                toReturn = Collections.singleton(new JITBPMNValidationResult(JITBPMNValidationResult.ERROR_LEVEL.SEVERE, error));
            } else {
                toReturn = new ArrayList<>();
                JITProcessValidationError[] processValidationErrors = validateProcesses(processes);
                for (JITProcessValidationError processValidationError : processValidationErrors) {
                    toReturn.add(getJITBPMNValidationResult(processValidationError));
                }
            }
        } catch (JITProcessParsingValidationException e) {
            toReturn = Collections.singleton(getJITBPMNValidationResult(e));
            LOGGER.error("Fail to validate", e);
        } catch (Throwable e) {
            String error = e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : e.toString();
            toReturn = Collections.singleton(new JITBPMNValidationResult(JITBPMNValidationResult.ERROR_LEVEL.SEVERE, error));
            LOGGER.error("Fail to validate", e);
        }
        return toReturn;
    }

    static JITProcessValidationError[] validateProcesses(Collection<Process> processes) {
        JITProcessValidationError[] toReturn = new JITProcessValidationError[0];
        for (Process toValidate : processes) {
            JITProcessValidationError[] toAdd = PROCESS_VALIDATOR.validateProcess(toValidate);
            JITProcessValidationError[] temp = new JITProcessValidationError[toReturn.length + toAdd.length];
            System.arraycopy(toReturn, 0, temp, 0, toReturn.length);
            System.arraycopy(toAdd, 0, temp, (temp.length - toAdd.length), toAdd.length);
            toReturn = temp;
        }
        return toReturn;
    }

    static Collection<Process> parseModelXml(String modelXML) {
        Resource r = new InputStreamResource(new ByteArrayInputStream(modelXML.getBytes()));
        return parseModelResource(r);
    }

    static Collection<Process> parseModelResource(Resource r) {
        try (Reader reader = r.getReader()) {
            XmlProcessReader xmlReader = new XmlProcessReader(
                    BPMN_SEMANTIC_MODULES,
                    Thread.currentThread().getContextClassLoader());
            return xmlReader.read(reader);
        } catch (SAXException | IOException e) {
            throw new RuntimeException("Could not parse " + r, e);
        }
    }

    static JITBPMNValidationResult getJITBPMNValidationResult(JITProcessValidationError exception) {
        return new JITBPMNValidationResult(JITBPMNValidationResult.ERROR_LEVEL.WARNING,
                exception.getNodeId(),
                exception.getNodeName(),
                exception.getProcess().getId(),
                exception.getMessage());
    }

    static JITBPMNValidationResult getJITBPMNValidationResult(JITProcessParsingValidationException exception) {
        return new JITBPMNValidationResult(JITBPMNValidationResult.ERROR_LEVEL.SEVERE,
                exception.getNodeId(),
                exception.getNodeName(),
                exception.getProcessId(),
                exception.getMessage());
    }

    static String getErrorString(ProcessValidationError processValidationError, String resourceUri) {
        Process failed = processValidationError.getProcess();
        String uri = resourceUri != null ? resourceUri : "(unknown)";
        return String.format(ERROR_TEMPLATE, uri, failed.getId(), failed.getName(), processValidationError.getMessage());
    }
}
