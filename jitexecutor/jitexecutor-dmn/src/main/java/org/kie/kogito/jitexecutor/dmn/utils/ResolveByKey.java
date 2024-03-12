/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.jitexecutor.dmn.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kie.api.io.Resource;

public class ResolveByKey {
    private final Map<String, Resource> resources;

    public ResolveByKey(Map<String, Resource> resources) {
        this.resources = new HashMap<>(resources);
    }

    public Reader readerByKey(String key) {
        try {
            return resources.get(key).getReader();
        } catch (IOException e) {
            throw new RuntimeException("Unable to operate ValidatorImportReaderResolver", e);
        }
    }

    public Collection<Reader> allReaders() {
        List<Reader> results = resources.values().stream().map(r -> {
            try {
                return r.getReader();
            } catch (IOException e) {
                throw new RuntimeException("Unable to open reader for resource.", e);
            }
        }).collect(Collectors.toList());
        return results;
    }
}
