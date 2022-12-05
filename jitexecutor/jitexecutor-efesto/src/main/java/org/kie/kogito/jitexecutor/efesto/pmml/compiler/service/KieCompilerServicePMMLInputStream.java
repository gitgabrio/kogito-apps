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
package org.kie.kogito.jitexecutor.efesto.pmml.compiler.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dmg.pmml.PMML;
import org.kie.efesto.common.api.identifiers.EfestoAppRoot;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.efesto.compilationmanager.api.model.EfestoCallableOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationContext;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoInputStreamResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilerService;
import org.kie.kogito.jitexecutor.efesto.model.EfestoValidationOutput;
import org.kie.kogito.jitexecutor.efesto.pmml.compiler.exceptions.ExceptionCollection;
import org.kie.kogito.jitexecutor.efesto.pmml.identifiers.KiePmmlComponentRoot;
import org.kie.kogito.jitexecutor.efesto.pmml.identifiers.PmmlIdFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For the moment being, use this for DMN "validation", since DMN does not have a code-generation phase
 */
public class KieCompilerServicePMMLInputStream implements KieCompilerService<EfestoCompilationOutput, EfestoCompilationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieCompilerServicePMMLInputStream.class);

    @Override
    public boolean canManageResource(EfestoResource toProcess) {
        return toProcess instanceof EfestoInputStreamResource && ((EfestoInputStreamResource) toProcess).getModelType().equalsIgnoreCase("pmml");
    }

    @Override
    public List<EfestoCompilationOutput> processResource(EfestoResource toProcess, EfestoCompilationContext context) {
        if (!canManageResource(toProcess)) {
            throw new KieCompilerServiceException(String.format("%s can not process %s",
                    this.getClass().getName(),
                    toProcess.getClass().getName()));
        }
        EfestoInputStreamResource inputStreamResource = (EfestoInputStreamResource) toProcess;
        try {
            PMML retrieved = PMMLValidator.validateInputStream(inputStreamResource.getContent());
            return retrieved.getModels().stream()
                    .map(model -> getDefaultEfestoCompilationOutput(inputStreamResource.getFileName(), model.getModelName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Failed to process PMML model", e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getModelType() {
        return "pmml";
    }

    // This should be declared in efesto interface
    public EfestoValidationOutput validateResource(EfestoResource toProcess) {
        if (!canManageResource(toProcess)) {
            throw new KieCompilerServiceException(String.format("%s can not try to validate %s",
                    this.getClass().getName(),
                    toProcess.getClass().getName()));
        }
        EfestoInputStreamResource inputStreamResource = (EfestoInputStreamResource) toProcess;
        String modelIdentifier = ((EfestoInputStreamResource) toProcess).getFileName();
        EfestoValidationOutput.STATUS status;
        List<String> messages;
        try {
            PMMLValidator.validateInputStream(inputStreamResource.getContent());
            status = EfestoValidationOutput.STATUS.OK;
            messages = Collections.emptyList();

        } catch (ExceptionCollection e) {
            String error = e.getMessage() == null || e.getMessage().isEmpty() ? e.getClass().getName() : e.getMessage();
            LOGGER.warn("Failed to validate PMML model due to {}", error);
            status = EfestoValidationOutput.STATUS.FAIL;
            messages = new ArrayList<>();
            for (Exception ex : e.getExceptions()) {
                String exerror = ex.getMessage() == null || ex.getMessage().isEmpty() ? ex.getClass().getName() : ex.getMessage();
                messages.add(exerror);
            }
        } catch (Exception e) {
            String error = e.getMessage() == null || e.getMessage().isEmpty() ? e.getClass().getName() : e.getMessage();
            LOGGER.warn("Failed to validate PMML model due to {}", error);
            status = EfestoValidationOutput.STATUS.FAIL;
            messages = Collections.singletonList(error);
        }
        return new EfestoValidationOutput(modelIdentifier, status, messages);
    }

    private static EfestoCompilationOutput getDefaultEfestoCompilationOutput(String fileName, String modelName) {
        return new EfestoCallableOutput() {

            private final ModelLocalUriId modelLocalUriId = new EfestoAppRoot()
                    .get(KiePmmlComponentRoot.class)
                    .get(PmmlIdFactory.class)
                    .get(fileName, modelName);

            @Override
            public ModelLocalUriId getModelLocalUriId() {
                return modelLocalUriId;
            }

            @Override
            public List<String> getFullClassNames() {
                return Collections.emptyList();
            }
        };
    }
}
