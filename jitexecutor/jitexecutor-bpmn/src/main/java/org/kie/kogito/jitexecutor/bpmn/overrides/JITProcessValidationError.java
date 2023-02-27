package org.kie.kogito.jitexecutor.bpmn.overrides;

import org.jbpm.process.core.validation.impl.ProcessValidationErrorImpl;
import org.kie.api.definition.process.Process;

public class JITProcessValidationError extends ProcessValidationErrorImpl {

    private long nodeId;
    private String nodeName;

    public JITProcessValidationError(Process process, String message) {
        super(process, message);
    }

    public JITProcessValidationError(long nodeId, String nodeName, Process process, String message) {
        super(process, message);
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }

    public long getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }
}
