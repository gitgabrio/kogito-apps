package org.kie.kogito.jitexecutor.efesto.model;

import org.kie.efesto.common.api.listener.EfestoListener;
import org.kie.efesto.runtimemanager.core.model.EfestoRuntimeContextImpl;
import org.kie.memorycompiler.KieMemoryCompiler;

public class JitExecutorRuntimeContext<T extends EfestoListener> extends EfestoRuntimeContextImpl<T> {

    private final String modelSource;

    public JitExecutorRuntimeContext(ClassLoader parentClassLoader, String modelSource) {
        super(new KieMemoryCompiler.MemoryCompilerClassLoader(parentClassLoader));
        this.modelSource = modelSource;
    }

    public String getModelSource() {
        return modelSource;
    }
}
