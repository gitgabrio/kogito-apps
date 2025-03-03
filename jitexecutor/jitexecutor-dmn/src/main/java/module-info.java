module jitexecutor.dmn {
    requires jitexecutor.common;
    requires jakarta.inject;
    requires org.slf4j;
    requires java.xml;
    requires jakarta.cdi;
    requires jakarta.ws.rs;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.kie.api;
    requires org.kie.internal.api;
    requires org.kie.dmn.api;
    requires org.kie.dmn.core;
    requires org.kie.dmn.feel;
    requires org.kie.dmn.openapi;
    requires org.kie.dmn.validation;
    requires org.kie.kogito.kogito.dmn;

    requires org.kie.kogito.trusty.service;
    requires org.kie.kogito.explainability.api;
}