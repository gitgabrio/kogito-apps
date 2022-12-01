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
package org.kie.kogito.jitexecutor.efesto.responses;

import java.io.Serializable;

import org.kie.efesto.runtimemanager.api.model.EfestoOutput;

public class JITEfestoResult implements Serializable {
    private String modelName;

    private EfestoOutput efestoResult;

    public JITEfestoResult() {
        // Intentionally blank.
    }

    public JITEfestoResult(String modelName, EfestoOutput efestoResult) {
        this.modelName = modelName;
        this.efestoResult = efestoResult;
    }

    public String getModelName() {
        return modelName;
    }

    public EfestoOutput getEfestoResult() {
        return efestoResult;
    }
}
