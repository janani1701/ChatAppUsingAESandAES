package com.app.chat;

import io.undertow.server.handlers.accesslog.AccessLogReceiver;

import java.util.logging.Logger;

public class LogReceiver implements AccessLogReceiver {

    private Logger logger;

    public LogReceiver(Class clazz) {
        logger = Logger.getLogger(clazz.getName());
    }

    @Override
    public void logMessage(String message) {
        logger.info(message);
    }
}