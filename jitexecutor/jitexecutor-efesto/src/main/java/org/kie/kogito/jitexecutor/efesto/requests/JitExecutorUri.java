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
package org.kie.kogito.jitexecutor.efesto.requests;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;

public class JitExecutorUri implements Serializable {

    private String fullPath;
    private String fileName;
    private String modelName;

    public JitExecutorUri() {
        // reflection
    }

    public JitExecutorUri(String fullPath, String modelName) {
        this.fullPath = fullPath;
        this.fileName = fullPath.contains("/") ? fullPath.substring(fullPath.lastIndexOf('/') + 1) : fullPath;
        this.modelName = modelName;
    }

    @JsonGetter
    public String fullPath() {
        return fullPath;
    }

    @JsonGetter
    public String fileName() {
        return fileName;
    }

    @JsonGetter
    public String modelName() {
        return modelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JitExecutorUri that = (JitExecutorUri) o;
        return Objects.equals(fullPath, that.fullPath) && Objects.equals(fileName, that.fileName) && Objects.equals(modelName, that.modelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullPath, fileName, modelName);
    }
}
