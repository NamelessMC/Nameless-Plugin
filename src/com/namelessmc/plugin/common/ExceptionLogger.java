package com.namelessmc.plugin.common;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionLogger {

    private final Logger logger;
    private final boolean singleLineExceptions;

    public ExceptionLogger(Logger logger, boolean singleLineExceptions) {
        this.logger = logger;
        this.singleLineExceptions = singleLineExceptions;
    }

    public void logException(Throwable t, Level level) {
        if (this.singleLineExceptions) {
            logger.log(level, t.getClass().getSimpleName() + " " + t.getMessage());
        } else {
            logger.log(level, t.getMessage(), t);
        }
    }

    public void logException(Throwable t) {
        logException(t, Level.SEVERE);
    }

}
