package org.kie.kogito.jitexecutor.bpmn.overrides;

import org.jbpm.bpmn2.core.*;
import org.jbpm.bpmn2.xml.*;
import org.jbpm.compiler.xml.Parser;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.exception.ActionExceptionHandler;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.core.context.swimlane.Swimlane;
import org.jbpm.process.core.correlation.CorrelationManager;
import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.process.core.event.MVELMessageExpressionEvaluator;
import org.jbpm.process.instance.impl.actions.SignalProcessInstanceAction;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.*;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.kogito.internal.process.runtime.KogitoNode;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JITProcessHandler extends ProcessHandler {

    private final Logger logger = LoggerFactory.getLogger(JITProcessHandler.class);
    private final ProcessHandler worker = new ProcessHandler();

    @Override
    public Object start(final String uri, final String localName,
                        final Attributes attrs, final Parser parser)
            throws SAXException {
        parser.startElementBuilder(localName, attrs);

        String id = attrs.getValue("id");
        String name = attrs.getValue("name");
        String visibility = attrs.getValue("processType");
        String packageName = attrs.getValue("http://www.jboss.org/drools", "packageName");
        String dynamic = attrs.getValue("http://www.jboss.org/drools", "adHoc");
        String version = attrs.getValue("http://www.jboss.org/drools", "version");

        RuleFlowProcess process = new RuleFlowProcess();
        process.setAutoComplete(true);
        process.setId(id);
        if (name == null) {
            name = id;
        }
        process.setName(name);
        process.setType(KogitoWorkflowProcess.BPMN_TYPE);
        if (packageName == null) {
            packageName = "org.drools.bpmn2";
        }
        process.setPackageName(packageName);
        if ("true".equals(dynamic)) {
            process.setDynamic(true);
            process.setAutoComplete(false);
        }
        if (version != null) {
            process.setVersion(version);
        }
        if (visibility == null || "".equals(visibility)) {
            visibility = KogitoWorkflowProcess.NONE_VISIBILITY;
        }
        process.setVisibility(visibility);
        ((ProcessBuildData) parser.getData()).setMetaData(CURRENT_PROCESS, process);
        ((ProcessBuildData) parser.getData()).addProcess(process);
        // register the definitions object as metadata of process.
        process.setMetaData("Definitions", parser.getParent());
        // register bpmn2 imports as meta data of process
        Object typedImports = ((ProcessBuildData) parser.getData()).getMetaData("Bpmn2Imports");
        if (typedImports != null) {
            process.setMetaData("Bpmn2Imports", typedImports);
        }
        // register item definitions as meta data of process
        Object itemDefinitions = ((ProcessBuildData) parser.getData()).getMetaData("ItemDefinitions");
        if (itemDefinitions != null) {
            process.setMetaData("ItemDefinitions", itemDefinitions);
        }

        // for unique id's of nodes, start with one to avoid returning wrong nodes for dynamic nodes
        parser.getMetaData().put("idGen", new AtomicInteger(1));
        parser.getMetaData().put("CurrentProcessDefinition", process);
        process.getCorrelationManager().setClassLoader(parser.getClassLoader());
        return process;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object end(final String uri, final String localName,
                      final Parser parser) throws SAXException {
        parser.endElementBuilder();

        RuleFlowProcess process = (RuleFlowProcess) parser.getCurrent();
        List<IntermediateLink> throwLinks = (List<IntermediateLink>) process
                .getMetaData(LINKS);
        linkIntermediateLinks(process, throwLinks);

        List<SequenceFlow> connections = (List<SequenceFlow>) process.getMetaData(CONNECTIONS);
        linkConnections(process, connections);
        linkBoundaryEvents(process);

        // This must be done *after* linkConnections(process, connections)
        //  because it adds hidden connections for compensations
        List<Association> associations = (List<Association>) process.getMetaData(ASSOCIATIONS);
        linkAssociations((Definitions) process.getMetaData("Definitions"), process, associations);

        List<Lane> lanes = (List<Lane>) process.getMetaData(LaneHandler.LANES);
        assignLanes(process, lanes);
        postProcessNodes(process, process);
        postProcessCollaborations(process, parser);
        return process;
    }


    private void assignLanes(RuleFlowProcess process, List<Lane> lanes) {
        List<String> laneNames = new ArrayList<>();
        Map<String, String> laneMapping = new HashMap<>();
        if (lanes != null) {
            for (Lane lane : lanes) {
                String name = lane.getName();
                if (name != null) {
                    Swimlane swimlane = new Swimlane();
                    swimlane.setName(name);
                    process.getSwimlaneContext().addSwimlane(swimlane);
                    laneNames.add(name);
                    for (String flowElementRef : lane.getFlowElements()) {
                        laneMapping.put(flowElementRef, name);
                    }
                }
            }
        }
        assignLanes(process, laneMapping);
    }

    private void assignLanes(NodeContainer nodeContainer, Map<String, String> laneMapping) {
        for (Node node : nodeContainer.getNodes()) {
            String lane = null;
            String uniqueId = (String) node.getMetaData().get("UniqueId");
            if (uniqueId != null) {
                lane = laneMapping.get(uniqueId);
            } else {
                lane = laneMapping.get(XmlBPMNProcessDumper.getUniqueNodeId(node));
            }
            if (lane != null) {
                ((NodeImpl) node).setMetaData("Lane", lane);
                if (node instanceof HumanTaskNode) {
                    ((HumanTaskNode) node).setSwimlane(lane);
                }
            }
            if (node instanceof NodeContainer) {
                assignLanes((NodeContainer) node, laneMapping);
            }
        }
    }

    private void postProcessNodes(RuleFlowProcess process, NodeContainer container) {
        List<String> eventSubProcessHandlers = new ArrayList<>();

        for (Node node : container.getNodes()) {
            try {
                if (node instanceof StateNode) {
                    StateNode stateNode = (StateNode) node;
                    String condition = (String) stateNode.getMetaData("Condition");
                    Constraint constraint = new ConstraintImpl();
                    constraint.setConstraint(condition);
                    constraint.setType("rule");
                    for (org.kie.api.definition.process.Connection connection : stateNode.getDefaultOutgoingConnections()) {
                        stateNode.setConstraint(connection, constraint);
                    }
                } else if (node instanceof NodeContainer) {
                    // prepare event sub process
                    if (node instanceof EventSubProcessNode) {
                        EventSubProcessNode eventSubProcessNode = (EventSubProcessNode) node;

                        Node[] nodes = eventSubProcessNode.getNodes();
                        for (Node subNode : nodes) {
                            // avoids cyclomatic complexity
                            if (subNode == null || !(subNode instanceof StartNode)) {
                                continue;
                            }
                            List<Trigger> triggers = ((StartNode) subNode).getTriggers();
                            if (triggers == null) {
                                continue;
                            }
                            for (Trigger trigger : triggers) {
                                if (trigger instanceof EventTrigger) {
                                    final List<EventFilter> filters = ((EventTrigger) trigger).getEventFilters();

                                    for (EventFilter filter : filters) {
                                        if (filter instanceof EventTypeFilter) {
                                            eventSubProcessNode.addEvent((EventTypeFilter) filter);

                                            String type = ((EventTypeFilter) filter).getType();
                                            if (type.startsWith("Error-") || type.startsWith("Escalation")) {
                                                String faultCode = (String) subNode.getMetaData().get("FaultCode");
                                                String replaceRegExp = "Error-|Escalation-";
                                                final String signalType = type;

                                                ExceptionScope exceptionScope =
                                                        (ExceptionScope) ((ContextContainer) eventSubProcessNode.getParentContainer()).getDefaultContext(ExceptionScope.EXCEPTION_SCOPE);
                                                if (exceptionScope == null) {
                                                    exceptionScope = new ExceptionScope();
                                                    ((ContextContainer) eventSubProcessNode.getParentContainer()).addContext(exceptionScope);
                                                    ((ContextContainer) eventSubProcessNode.getParentContainer()).setDefaultContext(exceptionScope);
                                                }
                                                String faultVariable = null;
                                                if (trigger.getInAssociations() != null && !trigger.getInAssociations().isEmpty()) {
                                                    faultVariable = findVariable(trigger.getInAssociations().get(0).getTarget().getLabel(), process.getVariableScope());
                                                }

                                                ActionExceptionHandler exceptionHandler = new ActionExceptionHandler();
                                                DroolsConsequenceAction action = new DroolsConsequenceAction("java", "");
                                                action.setMetaData("Action", new SignalProcessInstanceAction(signalType, faultVariable, null, SignalProcessInstanceAction.PROCESS_INSTANCE_SCOPE));
                                                exceptionHandler.setAction(action);
                                                exceptionHandler.setFaultVariable(faultVariable);
                                                if (faultCode != null) {
                                                    String trimmedType = type.replaceFirst(replaceRegExp, "");
                                                    exceptionScope.setExceptionHandler(trimmedType, exceptionHandler);
                                                    eventSubProcessHandlers.add(trimmedType);
                                                } else {
                                                    exceptionScope.setExceptionHandler(faultCode, exceptionHandler);
                                                }
                                            } else if (type.equals("Compensation")) {
                                                // 1. Find the parent sub-process to this event sub-process
                                                NodeContainer parentSubProcess = null;
                                                NodeContainer subProcess = eventSubProcessNode.getParentContainer();
                                                Object isForCompensationObj = eventSubProcessNode.getMetaData("isForCompensation");
                                                if (isForCompensationObj == null) {
                                                    eventSubProcessNode.setMetaData("isForCompensation", true);
                                                    logger.warn("Overriding empty value of \"isForCompensation\" attribute on Event Sub-Process [{}] and setting it to true.",
                                                            eventSubProcessNode.getMetaData("UniqueId"));
                                                }
                                                String compensationHandlerId = "";
                                                if (subProcess instanceof RuleFlowProcess) {
                                                    // If jBPM deletes the process (instance) as soon as the process completes..
                                                    // ..how do you expect to signal compensation on the completed process (instance)?!?
                                                    throw new ProcessParsingValidationException("Compensation Event Sub-Processes at the process level are not supported.");
                                                }
                                                if (subProcess instanceof Node) {
                                                    parentSubProcess = ((KogitoNode) subProcess).getParentContainer();
                                                    compensationHandlerId = (String) ((CompositeNode) subProcess).getMetaData(Metadata.UNIQUE_ID);
                                                }
                                                // 2. The event filter (never fires, purely for dumping purposes) has already been added

                                                // 3. Add compensation scope
                                                addCompensationScope(process, eventSubProcessNode, parentSubProcess, compensationHandlerId);
                                            }
                                        }
                                    }
                                } else if (trigger instanceof ConstraintTrigger) {
                                    ConstraintTrigger constraintTrigger = (ConstraintTrigger) trigger;

                                    if (constraintTrigger.getConstraint() != null) {
                                        String processId = ((RuleFlowProcess) container).getId();
                                        String type = "RuleFlowStateEventSubProcess-Event-" + processId + "-" + eventSubProcessNode.getUniqueId();
                                        EventTypeFilter eventTypeFilter = new EventTypeFilter();
                                        eventTypeFilter.setType(type);
                                        eventSubProcessNode.addEvent(eventTypeFilter);
                                    }
                                }
                            }
                        }
                    }
                    postProcessNodes(process, (NodeContainer) node);
                } else if (node instanceof EndNode) {
                    handleIntermediateOrEndThrowCompensationEvent((EndNode) node);
                } else if (node instanceof ActionNode) {
                    handleIntermediateOrEndThrowCompensationEvent((ActionNode) node);
                } else if (node instanceof EventNode) {
                    final EventNode eventNode = (EventNode) node;
                    if (!(eventNode instanceof BoundaryEventNode) && eventNode.getDefaultIncomingConnections().isEmpty()) {
                        throw new ProcessParsingValidationException("Event node '" + node.getName() + "' [" + node.getId() + "] has no incoming connection");
                    }
                }
            } catch (ProcessParsingValidationException e) {
                String processId = e.getProcessId() != null ? e.getProcessId() : process.getId();
                String nodeName = node.getName() != null ? node.getName() : "(unknown)";
                throw new JITProcessParsingValidationException(node.getId(), nodeName, processId, e.getMessage());
            }
        }

        // process fault node to disable terminate parent if there is event subprocess handler
        for (Node node : container.getNodes()) {
            try {
                if (node instanceof FaultNode) {
                    FaultNode faultNode = (FaultNode) node;
                    if (eventSubProcessHandlers.contains(faultNode.getFaultName())) {
                        faultNode.setTerminateParent(false);
                    }
                }
            } catch (ProcessParsingValidationException e) {
                String processId = e.getProcessId() != null ? e.getProcessId() : process.getId();
                String nodeName = node.getName() != null ? node.getName() : "(unknown)";
                throw new JITProcessParsingValidationException(node.getId(), nodeName, processId, e.getMessage());
            }
        }
    }

    private void postProcessCollaborations(RuleFlowProcess process, Parser parser) {
        // now we wire correlation process subscriptions
        CorrelationManager correlationManager = process.getCorrelationManager();
        for (Message message : HandlerUtil.messages(parser).values()) {
            correlationManager.newMessage(message.getId(), message.getName(), message.getType());
        }

        // only the ones this process is member of
        List<Collaboration> collaborations = HandlerUtil.collaborations(parser).values().stream().filter(c -> c.getProcessesRef().contains(process.getId())).collect(Collectors.toList());
        for (Collaboration collaboration : collaborations) {
            for (CorrelationKey key : collaboration.getCorrelationKeys()) {

                correlationManager.newCorrelation(key.getId(), key.getName());
                List<CorrelationProperty> properties = key.getPropertiesRef().stream().map(k -> HandlerUtil.correlationProperties(parser).get(k)).collect(Collectors.toList());
                for (CorrelationProperty correlationProperty : properties) {
                    correlationProperty.getMessageRefs().forEach(messageRef -> {

                        // for now only MVEL expressions
                        MVELMessageExpressionEvaluator evaluator = new MVELMessageExpressionEvaluator(correlationProperty.getRetrievalExpression(messageRef).getScript());
                        correlationManager.addMessagePropertyExpression(key.getId(), messageRef, correlationProperty.getId(), evaluator);
                    });
                }
            }
        }

        // we create the correlations
        for (CorrelationSubscription subscription : HandlerUtil.correlationSubscription(process).values()) {
            correlationManager.subscribeTo(subscription.getCorrelationKeyRef());
            for (Map.Entry<String, Expression> binding : subscription.getPropertyExpressions().entrySet()) {
                MVELMessageExpressionEvaluator evaluator = new MVELMessageExpressionEvaluator(binding.getValue().getScript());
                correlationManager.addProcessSubscriptionPropertyExpression(subscription.getCorrelationKeyRef(), binding.getKey(), evaluator);
            }
        }
    }


}
