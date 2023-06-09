package com.swirlds.base.logging.impl;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import com.swirlds.base.context.DiagnosticContextAccess;
import com.swirlds.base.context.impl.DiagnosticContextAccessProvider;
import com.swirlds.base.logging.Logger;
import java.time.LocalDateTime;

public class LoggerImpl implements Logger {

    private final String name;
    DiagnosticContextAccess diagnosticContextAccess = DiagnosticContextAccessProvider.getAccess();

    public LoggerImpl(String name) {
        this.name = name;
    }

    public void info(String message) {
        logMessage("INFO", message);
    }

    public void error(String message) {
        logMessage("ERROR", message);
    }

    private void logMessage(String level, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append(LocalDateTime.now().format(ISO_LOCAL_DATE_TIME));
        sb.append(" [");
        sb.append(Thread.currentThread().getName());
        sb.append("] ");
        sb.append(name);
        sb.append(" - ");
        sb.append(level);
        sb.append(" - ");
        sb.append(message);
        sb.append(" - {");
        diagnosticContextAccess.getKeys().forEach(key -> {
            sb.append(key);
            sb.append("=");
            sb.append(diagnosticContextAccess.getValue(key));
            sb.append(", ");
        });
        sb.append("}");
        System.out.println(sb.toString());
    }

}
