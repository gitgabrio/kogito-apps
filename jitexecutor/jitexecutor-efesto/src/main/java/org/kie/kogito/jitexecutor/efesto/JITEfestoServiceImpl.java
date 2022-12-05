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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.identifiers.NamedLocalUriId;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.kogito.jitexecutor.efesto.managers.EfestoCompilerManager;
import org.kie.kogito.jitexecutor.efesto.managers.EfestoRuntimeManager;
import org.kie.kogito.jitexecutor.efesto.model.EfestoValidationOutput;
import org.kie.kogito.jitexecutor.efesto.requests.JitExecutorUri;
import org.kie.kogito.jitexecutor.efesto.requests.MultipleResourcesPayload;
import org.kie.kogito.jitexecutor.efesto.requests.ResourceWithURI;
import org.kie.kogito.jitexecutor.efesto.responses.JITEfestoResult;
import org.kie.kogito.jitexecutor.efesto.responses.JITEfestoValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JITEfestoServiceImpl implements JITEfestoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JITEfestoServiceImpl.class);

    @Override
    public JITEfestoResult evaluateModel(MultipleResourcesPayload modelsPayload, Map<String, Object> inputData) {
        Map<JitExecutorUri, NamedLocalUriId> mappedUris = compileModels(modelsPayload);
        NamedLocalUriId localUriId = mappedUris.get(modelsPayload.getMainURI());
        if (localUriId == null) {
            return new JITEfestoResult(modelsPayload.getMainURI().modelName(), null);
        } else {
            return evaluateModel(localUriId, inputData);
        }
    }

    @Override
    public JITEfestoValidation validatePayload(MultipleResourcesPayload payload) {
        List<EfestoValidationOutput> validationOutputs = payload.getResources()
                .stream().map(this::validateModel)
                .collect(Collectors.toList());
        return new JITEfestoValidation(validationOutputs);
    }

    EfestoValidationOutput validateModel(ResourceWithURI resourceWithURI) {
        LOGGER.debug("validateModel {}", resourceWithURI);
        return EfestoCompilerManager.validateModel(resourceWithURI.getContent(),
                resourceWithURI.getURI().fileName());
    }

    JITEfestoResult evaluateModel(NamedLocalUriId localUriId, Map<String, Object> inputData) {
        EfestoOutput output = EfestoRuntimeManager.evaluateModel(localUriId, inputData);
        return new JITEfestoResult(localUriId.basePath(), output);
    }

    /**
     * Execute on-the-fly compilation of models, wherever is possible
     * 
     * @param modelsPayload
     */
    Map<JitExecutorUri, NamedLocalUriId> compileModels(MultipleResourcesPayload modelsPayload) {
        return modelsPayload.getResources().stream()
                .collect(Collectors.toMap(ResourceWithURI::getURI,
                        this::compileModel));
    }

    NamedLocalUriId compileModel(ResourceWithURI resourceWithURI) {
        LOGGER.debug("compileModel {}", resourceWithURI);
        ModelLocalUriId modelLocalUriId = EfestoCompilerManager.compileModel(resourceWithURI.getContent(),
                resourceWithURI.getURI().fileName());
        return new NamedLocalUriId(modelLocalUriId.asLocalUri(), resourceWithURI.getURI().fileName(),
                resourceWithURI.getURI().modelName());
    }
}
