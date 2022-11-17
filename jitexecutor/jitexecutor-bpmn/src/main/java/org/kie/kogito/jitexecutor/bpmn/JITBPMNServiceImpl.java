/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;

import org.drools.io.InputStreamResource;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.compiler.xml.core.SemanticModules;
import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.ruleflow.core.validation.RuleFlowProcessValidator;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.kogito.jitexecutor.bpmn.responses.JITBPMNValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

@ApplicationScoped
public class JITBPMNServiceImpl implements JITBPMNService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JITBPMNServiceImpl.class);

    private static final RuleFlowProcessValidator PROCESS_VALIDATOR = RuleFlowProcessValidator.getInstance();

    private static final SemanticModules BPMN_SEMANTIC_MODULES = new SemanticModules();

    static {
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNExtensionsSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNDISemanticModule());
    }

    @Override
    public JITBPMNValidationResult evaluateModel(String modelXML) {
        Collection<String> errors;
        Collection<Process> processes;
        try {
            processes = parseModelXml(modelXML);
            if (processes.isEmpty()) {
                errors = Collections.singleton("Failed to parse process");
            } else {
                errors = new ArrayList<>();
                ProcessValidationError[] processValidationErrors = PROCESS_VALIDATOR.validateProcess(processes.iterator().next());
                for (ProcessValidationError processValidationError : processValidationErrors) {
                    errors.add(processValidationError.getMessage());
                }
            }
        } catch (Exception e) {
            String error = e.getMessage() != null && !e.getMessage().isEmpty() ? e.getMessage() : e.toString();
            errors = Collections.singleton(error);
        }
        return new JITBPMNValidationResult(errors);
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
}