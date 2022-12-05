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
package org.kie.kogito.jitexecutor.efesto.dmn.runtime.service;

import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;

import org.kie.efesto.common.api.identifiers.ModelLocalUriId;
import org.kie.kogito.jitexecutor.efesto.storage.ContextStorage;

public class ImportResolver {

    private ImportResolver() {
    }

    public static Reader readerByKey(String key) {
        try {
            return getFromContextStorage(key);
        } catch (Exception e) {
            throw new RuntimeException("Unable to operate ValidatorImportReaderResolver", e);
        }
    }

    private static Reader getFromContextStorage(String key) {
        Optional<ModelLocalUriId> modelLocalUriId =
                ContextStorage.getAllModelLocalUriId().stream().filter(modelLocalUriId1 -> modelLocalUriId1.basePath().equals(key))
                        .findFirst();
        Reader toReturn = null;
        if (modelLocalUriId.isPresent()) {
            toReturn = new StringReader(ContextStorage.getEfestoCompilationContext(modelLocalUriId.get()).getModelSource());
        }
        return toReturn;
    }

}
