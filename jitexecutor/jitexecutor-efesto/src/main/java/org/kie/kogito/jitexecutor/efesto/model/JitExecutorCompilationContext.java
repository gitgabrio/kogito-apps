package org.kie.kogito.jitexecutor.efesto.model;

import org.kie.efesto.common.api.listener.EfestoListener;
import org.kie.efesto.compilationmanager.core.model.EfestoCompilationContextImpl;
import org.kie.memorycompiler.KieMemoryCompiler;

public class JitExecutorCompilationContext<T extends EfestoListener> extends EfestoCompilationContextImpl<T> {

    private final String modelSource;

    public JitExecutorCompilationContext(ClassLoader parentClassLoader, String modelSource) {
        super(new KieMemoryCompiler.MemoryCompilerClassLoader(parentClassLoader));
        this.modelSource = modelSource;
    }

    public String getModelSource() {
        return modelSource;
    }
}
