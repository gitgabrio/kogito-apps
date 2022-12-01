package org.kie.kogito.jitexecutor.efesto;

import org.kie.kogito.jitexecutor.efesto.requests.JitExecutorUri;
import org.kie.kogito.jitexecutor.efesto.requests.ResourceWithURI;

public class TestingUtils {


    public static ResourceWithURI getResourceWithURI(String fileName, String modelName, String content) {
        return new ResourceWithURI(getModelLocalUriId(fileName, modelName), content);
    }

    public static JitExecutorUri getModelLocalUriId(String fileName, String modelName) {
        return new JitExecutorUri(fileName, modelName);
    }
}
