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
package org.kie.kogito.jitexecutor.efesto.dmn.compiler.service;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.core.compiler.profiles.ExtendedDMNProfile;
import org.kie.dmn.validation.DMNValidator;
import org.kie.dmn.validation.DMNValidatorFactory;
import org.kie.efesto.common.api.identifiers.EfestoAppRoot;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.compilationmanager.api.exceptions.KieCompilerServiceException;
import org.kie.efesto.compilationmanager.api.model.EfestoCallableOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationContext;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationOutput;
import org.kie.efesto.compilationmanager.api.model.EfestoInputStreamResource;
import org.kie.efesto.compilationmanager.api.model.EfestoResource;
import org.kie.efesto.compilationmanager.api.service.KieCompilerService;
import org.kie.kogito.jitexecutor.efesto.dmn.identifiers.DmnIdFactory;
import org.kie.kogito.jitexecutor.efesto.dmn.identifiers.KieDmnComponentRoot;

/**
 * For the moment being, use this for DMN "validation", since DMN does not have a code-generation phase
 */
public class KieCompilerServiceDMNInputStream implements KieCompilerService<EfestoCompilationOutput,
        EfestoCompilationContext> {


    static final DMNValidator validator = DMNValidatorFactory.newValidator(Arrays.asList(new ExtendedDMNProfile()));

    @Override
    public boolean canManageResource(EfestoResource toProcess) {
        return toProcess instanceof EfestoInputStreamResource && ((EfestoInputStreamResource) toProcess).getModelType().equalsIgnoreCase("dmn");
    }

    @Override
    public List<EfestoCompilationOutput> processResource(EfestoResource toProcess, EfestoCompilationContext context) {
        if (!canManageResource(toProcess)) {
            throw new KieCompilerServiceException(String.format("%s can not process %s",
                                                                this.getClass().getName(),
                                                                toProcess.getClass().getName()));
        }
        EfestoInputStreamResource inputStreamResource = (EfestoInputStreamResource)toProcess;
        List<DMNMessage> messages = validator.validate(new InputStreamReader(inputStreamResource.getContent()), DMNValidator.Validation.VALIDATE_SCHEMA, DMNValidator.Validation.VALIDATE_MODEL, DMNValidator.Validation.VALIDATE_COMPILATION, DMNValidator.Validation.ANALYZE_DECISION_TABLE);
        return Collections.singletonList(getDefaultEfestoCompilationOutput(inputStreamResource.getFileName(), inputStreamResource.getModelType()));
    }

    @Override
    public String getModelType() {
        return "dmn";
    }

    private static EfestoCompilationOutput getDefaultEfestoCompilationOutput(String fileName, String modelName) {
        return new EfestoCallableOutput() {

            private final ModelLocalUriId modelLocalUriId = new EfestoAppRoot()
                        .get(KieDmnComponentRoot.class)
                        .get(DmnIdFactory.class)
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
