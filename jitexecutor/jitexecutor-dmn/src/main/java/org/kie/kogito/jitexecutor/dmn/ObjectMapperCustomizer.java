package org.kie.kogito.jitexecutor.dmn;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperCustomizer extends Comparable<ObjectMapperCustomizer> {
    int MINIMUM_PRIORITY = Integer.MIN_VALUE;
    int QUARKUS_CUSTOMIZER_PRIORITY = -2147483548;
    int DEFAULT_PRIORITY = 0;

    void customize(ObjectMapper objectMapper);

    default int priority() {
        return 0;
    }

    default int compareTo(ObjectMapperCustomizer o) {
        return Integer.compare(o.priority(), this.priority());
    }
}
