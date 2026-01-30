
package com.neon.copper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

public final class LoggingGuard {

    public static void verifyLog4j() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration cfg = ctx.getConfiguration();

        if (cfg instanceof DefaultConfiguration) {
            System.err.println("FATAL: Log4j2 using DefaultConfiguration, exiting.");
            System.exit(2);
        }

        if (cfg.getAppenders() == null || cfg.getAppenders().isEmpty()) {
            System.err.println("FATAL: Log4j2 dosen't have appender, exiting.");
            System.exit(3);
        }
        boolean hasConsole = cfg.getAppenders().values().stream()
                .anyMatch(a -> a.getClass().getSimpleName().contains("Console"));
        boolean hasFile = cfg.getAppenders().values().stream()
                .anyMatch(a -> a.getClass().getSimpleName().contains("File"));

        if (!hasConsole && !hasFile) {
            System.err.println("FATAL: Log4j2 reported error during initialization, exiting.");
            System.exit(4);
        }
        Logger test = LogManager.getLogger("StartupCheck");
        test.info("Log4j2 initialization OK");
    }
}

