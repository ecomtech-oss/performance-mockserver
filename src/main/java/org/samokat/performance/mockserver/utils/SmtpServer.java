package org.samokat.performance.mockserver.utils;

import com.dumbster.smtp.SimpleSmtpServer;
import java.io.IOException;
import org.samokat.performance.mockserver.core.MockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmtpServer implements Runnable {

    private final SimpleSmtpServer server;

    Logger log = LoggerFactory.getLogger(MockServer.class);


    public SmtpServer() throws IOException {
        int SMTP_PORT = Integer.parseInt(System.getProperty("SMTP_PORT"));
        server = SimpleSmtpServer.start(SMTP_PORT);
        log.info("SMTP service is ready for emails");
    }


    @Override
    public void run() {
        if (this.server.getReceivedEmails().size() > 0) {
            log.info("Received " + this.server.getReceivedEmails().size());
            server.reset();
        }
    }

}
