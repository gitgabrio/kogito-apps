package org.kie.kogito.jitexecutor.efesto;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.efesto.common.api.identifiers.NamedLocalUriId;
import org.kie.efesto.compilationmanager.api.model.EfestoCompilationContext;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextUtils;
import org.kie.efesto.runtimemanager.api.model.EfestoOutput;
import org.kie.efesto.runtimemanager.api.model.EfestoRuntimeContext;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextUtils;
import org.kie.kogito.jitexecutor.efesto.managers.EfestoCompilerManager;
import org.kie.kogito.jitexecutor.efesto.managers.EfestoRuntimeManager;
import org.kie.kogito.jitexecutor.efesto.requests.MultipleResourcesPayload;
import org.kie.kogito.jitexecutor.efesto.requests.ResourceWithURI;
import org.kie.kogito.jitexecutor.efesto.responses.JITEfestoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JITEfestoServiceImpl implements JITEfestoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JITEfestoServiceImpl.class);

    @Override
    public JITEfestoResult evaluateModel(MultipleResourcesPayload modelsPayload, Map<String, Object> inputData) {
        Map<ResourceWithURI, NamedLocalUriId> mappedUris = compileModels(modelsPayload);
        NamedLocalUriId localUriId = mappedUris.keySet().stream()
                .filter(resourceWithURI -> resourceWithURI.getURI().equals(modelsPayload.getMainURI()))
                .findFirst()
                .map(mappedUris::get)
                .orElse(null);
        if (localUriId == null) {
            return new JITEfestoResult(modelsPayload.getMainURI().modelName(), null);
        } else {
            return evaluateModel(localUriId, inputData);
        }
    }

    JITEfestoResult evaluateModel(NamedLocalUriId localUriId, Map<String, Object> inputData) {
        EfestoOutput output = EfestoRuntimeManager.evaluateModel(localUriId, inputData);
        return new JITEfestoResult(localUriId.basePath(), output);
    }

    /**
     * Execute on-the-fly compilation of models, wherever is possible
     * @param modelsPayload
     */
    Map<ResourceWithURI, NamedLocalUriId> compileModels(MultipleResourcesPayload modelsPayload) {
        return modelsPayload.getResources().stream()
                .collect(Collectors.toMap(resourceWithURI -> resourceWithURI,
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
