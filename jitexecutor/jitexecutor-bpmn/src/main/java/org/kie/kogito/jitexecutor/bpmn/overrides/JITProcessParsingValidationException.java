package org.kie.kogito.jitexecutor.bpmn.overrides;

import org.jbpm.bpmn2.xml.ProcessParsingValidationException;
import org.kie.api.definition.process.Node;
import org.kie.kogito.jitexecutor.bpmn.responses.JITBPMNValidationResult;

public class JITProcessParsingValidationException extends ProcessParsingValidationException {

    private long nodeId;
    private String nodeName;


    public JITProcessParsingValidationException(String message) {
        super(message);
    }

    public JITProcessParsingValidationException(String processId, String message) {
        super(processId, message);
    }

    public JITProcessParsingValidationException(long nodeId, String nodeName, String processId, String message) {
        super(processId, message);
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
