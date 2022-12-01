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
package org.kie.kogito.jitexecutor.efesto.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.kie.efesto.common.api.exceptions.KieEfestoCommonException;
import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.identifiers.NamedLocalUriId;
import org.kie.efesto.common.api.model.GeneratedResources;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextImpl;
import org.kie.efesto.runtimemanager.api.exceptions.KieRuntimeServiceException;
import org.kie.efesto.runtimemanager.api.model.BaseEfestoInput;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.api.service.RuntimeManager;
import org.kie.efesto.runtimemanager.api.utils.SPIUtils;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextImpl;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.kie.kogito.jitexecutor.efesto.model.JitExecutorCompilationContext;
import org.kie.kogito.jitexecutor.efesto.model.JitExecutorRuntimeContext;
import org.kie.kogito.jitexecutor.efesto.storage.ContextStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EfestoRuntimeManager {

    private static final RuntimeManager runtimeManager;

    private static final Logger logger = LoggerFactory.getLogger(EfestoRuntimeManager.class);

    private static final String CHECK_CLASSPATH = "check classpath and dependencies!";

    static {
        runtimeManager = SPIUtils.getRuntimeManager(false).orElseThrow(() -> new RuntimeException("Failed to retrieve" +
                                                                                                          " RuntimeManager"));
    }

    public static EfestoOutput evaluateModel(NamedLocalUriId namedLocalUriId, Map<String, Object> inputData) {
        BaseEfestoInput<Map<String, Object>> efestoInput = new BaseEfestoInput<>(namedLocalUriId, inputData);

        JitExecutorRuntimeContext runtimeContext = getEfestoRuntimeContext(namedLocalUriId);
        try {
            Collection<EfestoOutput> retrieved = runtimeManager.evaluateInput(runtimeContext, efestoInput);
            if (retrieved.isEmpty()) {
                throw new KieEfestoCommonException("Failed to retrieve result for modelLocalUriId");
            } else {
                return retrieved.iterator().next();
            }
        } catch (Exception t) {
            String errorMessage = String.format("Evaluation error for %s@%s using %s due to %s: please %s",
                                                efestoInput.getModelLocalUriId(),
                                                efestoInput.getInputData(),
                                                efestoInput,
                                                t.getMessage(),
                                                CHECK_CLASSPATH);
            logger.error(errorMessage);
            throw new KieRuntimeServiceException(errorMessage, t);
        }
    }

    private EfestoRuntimeManager() {
    }

    private static JitExecutorRuntimeContext getEfestoRuntimeContext(ModelLocalUriId modelLocalUriId) {
        JitExecutorRuntimeContext toReturn = ContextStorage.getEfestoRuntimeContext(modelLocalUriId);
        if (toReturn == null) {
            toReturn = instantiateRuntimeContextFromCompilationContext(modelLocalUriId);
        }
        return toReturn;
    }

    private static JitExecutorRuntimeContext instantiateRuntimeContextFromCompilationContext(ModelLocalUriId modelLocalUriId) {
        JitExecutorCompilationContext compilationContext = ContextStorage.getEfestoCompilationContext(modelLocalUriId);
        if (compilationContext == null) {
            String errorMessage = String.format("Failed to retrieve EfestoCompilationContext for %s", modelLocalUriId);
            logger.error(errorMessage);
            throw new KieRuntimeServiceException(errorMessage);
        }
        Map<String, byte[]> generatedClasses = compilationContext.getGeneratedClasses(modelLocalUriId);
        if (generatedClasses == null) {
            String warnMessage = String.format("There are no generatedClasses for %s", modelLocalUriId);
            logger.warn(warnMessage);
            generatedClasses = Collections.EMPTY_MAP;
        }
        String model = modelLocalUriId.model();
        GeneratedResources generatedResources =
                (GeneratedResources) compilationContext.getGeneratedResourcesMap().get(model);
        if (generatedResources == null) {
            String errorMessage = String.format("Failed to retrieve GeneratedResources for %s", modelLocalUriId);
            logger.error(errorMessage);
            throw new KieRuntimeServiceException(errorMessage);
        }
        JitExecutorRuntimeContext toReturn = new JitExecutorRuntimeContext(Thread.currentThread().getContextClassLoader(), compilationContext.getModelSource());
        toReturn.addGeneratedClasses(modelLocalUriId, generatedClasses);
        toReturn.getGeneratedResourcesMap().put(model, generatedResources);
        ContextStorage.putEfestoRuntimeContext(modelLocalUriId, toReturn);
        return toReturn;
    }

/*    private static BaseEfestoInput<EfestoMapInputDTO> getMapInput(ModelLocalUriId modelLocalUriId,
                                                                  Map<String, Object> inputData) {
        EfestoMapInputDTO efestoMapInputDTO = modelLocalUriId.model().equals("drl") ? getDrlMapInput(inputData) : getSimpleMapInput(inputData);
        return new BaseEfestoInput<>(modelLocalUriId, efestoMapInputDTO);
    }*/

/*    private static EfestoMapInputDTO getSimpleMapInput(Map<String, Object> inputData) {
        return new EfestoMapInputDTO(null, null, inputData, null, null, null);
    }*/

/*    private static EfestoMapInputDTO getDrlMapInput(Map<String, Object> inputData) {
        List<Object> inserts = new ArrayList<>();
        if (inputData.containsKey("inserts")) {
            inserts = (List<Object>) inputData.get("inserts");
            inputData.remove("inserts");
        }
        Map<String, Object> globals = new HashMap<>();
        if (inputData.containsKey("globals")) {
            globals = (Map<String, Object>) inputData.get("globals");
            inputData.remove("globals");
        }
        String packageName = (String) inputData.get("package");
        inputData.remove("package");
        final Map<String, EfestoOriginalTypeGeneratedType> fieldTypeMap = new HashMap<>();
        inputData.forEach((s, o) -> {
            String objectType = o.getClass().getCanonicalName();
            EfestoOriginalTypeGeneratedType toPut = new EfestoOriginalTypeGeneratedType(objectType, objectType);
            fieldTypeMap.put(s, toPut);
        });
        return new EfestoMapInputDTO(inserts, globals, inputData, fieldTypeMap, "modl", packageName);
    }*/


}
