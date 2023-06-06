package com.swirlds.base.context;

import com.swirlds.base.context.impl.ThreadContextImpl;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

public class ThreadContext {

    private static final ThreadLocal<ThreadContextImpl> threadLocal = new ThreadLocal<>();

    public static void addMetadata(final String name, final String value) {
        getOrCreateThreadContext().addMetadata(name, value);
    }

    public static void removeMetadata(final String name) {
        getOrCreateThreadContext().removeMetadata(name);
    }

    public static String getValue(final String name) {
        return getOrCreateThreadContext().getValue(name);
    }

    public static Set<String> getKeys() {
        return getOrCreateThreadContext().getKeys();
    }

    private static ThreadContextImpl getOrCreateThreadContext() {
        ThreadContextImpl threadContext = threadLocal.get();
        if(threadContext == null) {
            threadContext = new ThreadContextImpl();
            threadLocal.set(threadContext);
        }
        return threadContext;
    }

    public static void runInMetadataContext(final String name, final String value, Runnable runnable) {
        getOrCreateThreadContext().addMetadata(name, value);
        try {
            runnable.run();
        } finally {
            getOrCreateThreadContext().removeMetadata(name);
        }
    }

    public static <T> T runInMetadataContext(final String name, final String value, Callable<T> callable)
            throws Exception {
        getOrCreateThreadContext().addMetadata(name, value);
        try {
            return callable.call();
        } finally {
            getOrCreateThreadContext().removeMetadata(name);
        }
    }
}
