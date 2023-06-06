package com.swirlds.base.logging;

import com.swirlds.base.logging.impl.LoggerImpl;

public interface Logger {

    void info(String message);

    void error(String message);

    static Logger getLogger(Class<?> cls) {
        return new LoggerImpl(cls.getName());
    }

}
