package org.kie.kogito.jitexecutor.bpmn.overrides;

import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.validation.RuleFlowProcessValidator;
import org.kie.api.definition.process.Process;

import java.util.Arrays;
import java.util.List;

public class JITRuleFlowProcessValidator extends RuleFlowProcessValidator {

    private static JITRuleFlowProcessValidator INSTANCE;
    protected JITRuleFlowProcessValidator() {
    }

    public static JITRuleFlowProcessValidator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JITRuleFlowProcessValidator();
        }
        return INSTANCE;
    }

    @Override
    public JITProcessValidationError[] validateProcess(Process process) {
        if (!(process instanceof RuleFlowProcess)) {
            throw new IllegalArgumentException(
                    "This validator can only validate ruleflow processes!");
        }
        return validateProcess((RuleFlowProcess) process);
    }

    public JITProcessValidationError[] validateProcess(final RuleFlowProcess process) {
        final ProcessValidationError[] errors = super.validateProcess(process);
        return Arrays.stream(errors)
                .map(this::convertProcessValidationError).toArray(JITProcessValidationError[]::new);
    }

    protected JITProcessValidationError convertProcessValidationError(ProcessValidationError toConvert) {
        return  toConvert instanceof JITProcessValidationError ? (JITProcessValidationError) toConvert :
                new JITProcessValidationError(toConvert.getProcess(), toConvert.getMessage());
    }

    @Override
    protected void addErrorMessage(RuleFlowProcess process,
                                   org.kie.api.definition.process.Node node,
                                   List<ProcessValidationError> errors,
                                   String message) {
        String error = String.format("Node '%s' [%d] %s",
                node.getName(),
                node.getId(),
                message);
        String nodeName = node.getName() != null ? node.getName() : "(unknown)";
        errors.add(new JITProcessValidationError(node.getId(), nodeName, process,
                error));
    }
}
