package org.kie.kogito.jitexecutor.bpmn.overrides;

import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.xml.Handler;

public class JITBPMNSemanticModule extends BPMNSemanticModule {


    public JITBPMNSemanticModule() {
        removeHandler("process");
        addHandler("process", new JITProcessHandler());
    }

    private void removeHandler(String name) {
        if (handlers.containsKey(name)) {
            Handler handler = handlers.get(name);
            this.handlers.remove(name);
            if (handler != null && handler.generateNodeFor() != null) {
                this.handlersByClass.remove(handler.generateNodeFor());
            }
        }
    }
}
