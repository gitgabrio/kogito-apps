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
package org.kie.kogito.jitexecutor.bpmn.responses;

import java.io.Serializable;

public class JITBPMNValidationResult implements Serializable {

    public enum ERROR_LEVEL {
        FINE,
        WARNING,
        SEVERE
    }

    private ERROR_LEVEL errorLevel;
    private long nodeId;
    private String nodeName;
    private String processId;
    private String errorMessage;

    public JITBPMNValidationResult() {
    }

    public JITBPMNValidationResult(ERROR_LEVEL errorLevel, String errorMessage) {
        this(errorLevel, -1, "", "", errorMessage);
    }

    public JITBPMNValidationResult(ERROR_LEVEL errorLevel, long nodeId, String nodeName, String processId, String errorMessage) {
        this.errorLevel = errorLevel;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.processId = processId;
        this.errorMessage = errorMessage;
    }

    public ERROR_LEVEL getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(ERROR_LEVEL errorLevel) {
        this.errorLevel = errorLevel;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
