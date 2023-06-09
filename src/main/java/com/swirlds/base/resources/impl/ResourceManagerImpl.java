package com.swirlds.base.resources.impl;

import com.swirlds.base.context.DiagnosticContext;
import com.swirlds.base.logging.Logger;
import com.swirlds.base.resources.ExecutorWithSpanSupport;
import com.swirlds.base.resources.ResourceManager;
import com.swirlds.base.span.Span;
import com.swirlds.base.span.SpanDefinition;
import com.swirlds.base.span.SpanFactoryProvider;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ResourceManagerImpl implements ResourceManager {

    private final static Logger logger = Logger.getLogger(ResourceManagerImpl.class);

    private static final ResourceManagerImpl INSTANCE = new ResourceManagerImpl();

    private String name = "ResourceManager-" + UUID.randomUUID().toString();

    private ResourceManagerImpl() {
    }

    public static ResourceManagerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public ExecutorWithSpanSupport getExecutor() {
        final Executor innerExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, name + "-Thread-1");
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        logger.error(
                                "Uncaught exception in ResourceManager thread [" + t.getName() + "]:" + e.getMessage());
                    }
                });
                return thread;
            }
        });
        return new ExecutorWithSpanSupport() {
            @Override
            public void execute(Runnable command, SpanDefinition childSpanDefinition, String data, Span parentSpan) {
                innerExecutor.execute(() -> {
                    DiagnosticContext diagnosticContext = DiagnosticContext.getCurrent();
                    diagnosticContext.addMetadata("ResourceManager", name);
                    diagnosticContext.addMetadata("Thread", Thread.currentThread().getName());
                    try {
                        if (childSpanDefinition != null) {
                            Span childSpan = SpanFactoryProvider.createSpanFactory(childSpanDefinition)
                                    .create(parentSpan, data);
                            try {
                                command.run();
                                childSpan.commit();
                            } catch (Exception e) {
                                childSpan.fail();
                                throw e;
                            }
                        } else {
                            command.run();
                        }
                    } finally {
                        diagnosticContext.removeMetadata("ResourceManager");
                        diagnosticContext.removeMetadata("Thread");
                    }
                });
            }
        };
    }
}
