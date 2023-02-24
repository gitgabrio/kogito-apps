package org.kie.kogito.jitexecutor.bpmn.overrides;

import org.jbpm.bpmn2.xml.ProcessParsingValidationException;
import org.kie.api.definition.process.Node;

public class JITProcessParsingValidationException extends ProcessParsingValidationException {

    private Node node;
    public JITProcessParsingValidationException(String message) {
        super(message);
    }

    public JITProcessParsingValidationException(String processId, String message) {
        super(processId, message);
    }

    public JITProcessParsingValidationException(String message, Node node) {
        super(message);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
