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

import org.kie.kogito.jitexecutor.efesto.requests.JitExecutorUri;
import org.kie.kogito.jitexecutor.efesto.requests.ResourceWithURI;

public class TestingUtils {

    public static final String PMML_FILE = "test_regression.pmml";

    public static final String PMML_FILE_INVALID = "test_invalid.pmml";
    public static final String DMN_FILE = "test.dmn";
    public static final String DMN_FILE_INVALID = "test_invalid.dmn";
    public static final String DMN_PMML_FILE = "KiePMMLRegression.dmn";
    public static final String PMML_MODEL_NAME = "LinReg";

    public static ResourceWithURI getResourceWithURI(String fileName, String modelName, String content) {
        return new ResourceWithURI(getModelLocalUriId(fileName, modelName), content);
    }

    public static JitExecutorUri getModelLocalUriId(String fileName, String modelName) {
        return new JitExecutorUri(fileName, modelName);
    }
}
