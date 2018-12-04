
package org.riversun.jetty.basicauth;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public final class LogSetup {

    protected static final String LOGGING_PROPERTIES_FILE = "logging.properties";

    public static void enableLogging() {
        try {
            final InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(LOGGING_PROPERTIES_FILE);

            if (inStream != null) {

                LogManager.getLogManager().readConfiguration(inStream);

            } else {
                System.err.println("logging properties not found.");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
