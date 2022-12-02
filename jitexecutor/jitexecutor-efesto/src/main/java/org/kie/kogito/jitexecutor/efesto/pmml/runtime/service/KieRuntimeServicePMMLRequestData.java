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
package org.kie.kogito.jitexecutor.efesto.pmml.runtime.service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.efesto.common.api.cache.EfestoClassKey;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.KieRuntimeService;
import org.kie.kogito.jitexecutor.efesto.model.JitExecutorRuntimeContext;
import org.kie.kogito.jitexecutor.efesto.pmml.model.EfestoOutputPMML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.efesto.runtimemanager.api.utils.GeneratedResourceUtils.isPresentExecutableOrRedirect;
import static org.kie.kogito.jitexecutor.efesto.managers.EfestoRuntimeManager.getEfestoRuntimeContext;

public class KieRuntimeServicePMMLRequestData implements KieRuntimeService<PMMLRequestData, PMML4Result, EfestoInput<PMMLRequestData>, EfestoOutputPMML, EfestoRuntimeContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieRuntimeServicePMMLRequestData.class);

    @Override
    public EfestoClassKey getEfestoClassKeyIdentifier() {
        return new EfestoClassKey(BaseEfestoInput.class, PMMLRequestData.class);
    }

    @Override
    public boolean canManageInput(EfestoInput toEvaluate, EfestoRuntimeContext context) {
        return canManageEfestoInput(toEvaluate, context);
    }

    @Override
    public Optional<EfestoOutputPMML> evaluateInput(EfestoInput<PMMLRequestData> toEvaluate,
            EfestoRuntimeContext context) {
        if (!canManageInput(toEvaluate, context)) {
            throw new KieRuntimeServiceException("Wrong parameters " + toEvaluate + " " + context);
        }
        // Temporary workaround
        // This logic should be moved elsewhere
        JitExecutorRuntimeContext jitExecutorRuntimeContext = getJitExecutorRuntimeContext(context, toEvaluate.getModelLocalUriId());
        String modelSource = jitExecutorRuntimeContext.getModelSource();
        return Optional.ofNullable(evaluateInput(modelSource, toEvaluate.getModelLocalUriId(), toEvaluate.getInputData()));
    }

    @Override
    public String getModelType() {
        return "pmml";
    }

    private boolean canManageEfestoInput(EfestoInput toEvaluate, EfestoRuntimeContext runtimeContext) {
        return isPresentExecutableOrRedirect(toEvaluate.getModelLocalUriId(), runtimeContext);
    }

    private EfestoOutputPMML evaluateInput(String modelSource, ModelLocalUriId modelLocalUriId, PMMLRequestData inputData) {
        PMML4Result toReturn;
        try {
            Map<String, Object> mapInput = inputData.getMappedRequestParams().entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> entry.getValue().getValue()));
            toReturn = PMMLEvaluator.evaluateString(modelSource, mapInput);
        } catch (Exception e) {
            LOGGER.error("Failed to evaluate {}", inputData, e);
            toReturn = new PMML4Result();
            toReturn.setResultCode("FAIL");
        }
        return new EfestoOutputPMML(modelLocalUriId, toReturn);
    }

    private JitExecutorRuntimeContext getJitExecutorRuntimeContext(EfestoRuntimeContext context, ModelLocalUriId modelLocalUriId) {
        if (context instanceof JitExecutorRuntimeContext) {
            return (JitExecutorRuntimeContext) context;
        } else {
            return getEfestoRuntimeContext(modelLocalUriId);
        }
    }
}
