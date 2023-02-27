/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.jitexecutor.bpmn.responses;

import java.util.Collection;

public class MultipleResponsesPayload {

    private String mainURI;
    private Collection<JITBPMNValidationResult> results;

    public MultipleResponsesPayload() {
    }

    public MultipleResponsesPayload(String mainURI, Collection<JITBPMNValidationResult> results) {
        this.mainURI = mainURI;
        this.results = results;
    }

    public String getMainURI() {
        return mainURI;
    }

    public void setMainURI(String mainURI) {
        this.mainURI = mainURI;
    }

    public Collection<JITBPMNValidationResult> getResults() {
        return results;
    }

    public void setResults(Collection<JITBPMNValidationResult> results) {
        this.results = results;
    }
}
