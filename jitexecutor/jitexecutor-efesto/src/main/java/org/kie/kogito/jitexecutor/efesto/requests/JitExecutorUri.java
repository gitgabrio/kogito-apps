package org.kie.kogito.jitexecutor.efesto.requests;

import java.io.Serializable;

public class JitExecutorUri implements Serializable {

    private String fullPath;
    private String fileName;
    private String modelName;

    public JitExecutorUri(String fullPath, String modelName) {
        this.fullPath = fullPath;
        this.fileName = fullPath.contains("/") ? fullPath.substring(fullPath.lastIndexOf('/')+ 1) : fullPath;
        this.modelName = modelName;
    }

    public String fullPath() {
        return fullPath;
    }

    public String fileName() {
        return fileName;
    }

    public String modelName() {
        return modelName;
    }
}
