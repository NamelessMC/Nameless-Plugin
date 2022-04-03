package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.jetbrains.annotations.NotNull;

public class ExceptionLogger {

    private final @NotNull AbstractLogger logger;
    private final boolean singleLineExceptions;

    public ExceptionLogger(final @NotNull AbstractLogger logger,
                           final boolean singleLineExceptions) {
        this.logger = logger;
        this.singleLineExceptions = singleLineExceptions;
    }



}
