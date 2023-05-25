module jitexecutor.bpmn {
    requires jitexecutor.common;
    requires java.ws.rs;
    requires jakarta.inject;
    requires org.slf4j;
    requires java.xml;
    requires jakarta.cdi;
    requires org.kie.api;
    requires org.drools.io;
    requires org.kie.kogito.jbpm.flow.builder;
    requires org.kie.kogito.jbpm.bpmn2;
    requires org.kie.kogito.jbpm.flow.core;
}