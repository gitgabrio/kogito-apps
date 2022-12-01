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
package org.kie.kogito.jitexecutor.efesto.pmml.identifiers;

import static org.kie.efesto.common.api.identifiers.LocalUri.SLASH;

public class PmmlIdFactory implements PmmlComponentRoot {

    public LocalComponentIdPmml get(String fileName, String modelName) {
        if (fileName.contains(SLASH)) {
            fileName = fileName.substring(fileName.lastIndexOf(SLASH) + 1);
        }
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return new LocalComponentIdPmml(fileName, modelName);
    }

}
