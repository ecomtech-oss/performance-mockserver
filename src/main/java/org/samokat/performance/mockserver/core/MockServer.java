package org.samokat.performance.mockserver.core;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.samokat.performance.mockserver.core.initializer.CommandSwitcher;
import org.samokat.performance.mockserver.utils.SmtpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockServer {

    private static ClientAndServer mockServer;

    static Logger log = LoggerFactory.getLogger(MockServer.class);


    public static void main(String[] args) {
        try {
            var mockType = System.getProperty("team").toLowerCase();
            var logLevel = System.getProperty("loglevel").toUpperCase();
            int smtpDelaySeconds = Integer.parseInt(System.getProperty("SMTP_DELAY_SECONDS"));
            log.info(mockType);
            startMockServer(logLevel);
            mockServer.upsert(
                Objects.requireNonNull(CommandSwitcher.getCommand(mockType))
                    .initializeExpectations());
            log.info("Mockserver is running");

            ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1);
            threadPool.scheduleWithFixedDelay(new SmtpServer(), 0, smtpDelaySeconds,
                TimeUnit.SECONDS);
        } catch (NullPointerException | IOException e) {
            log.error("Fatal error in initialization process", e);
            System.exit(1);
        }
    }


    public static void startMockServer(String logLevel) throws IOException {
        var clientPort = Integer.parseInt(System.getProperty("CLIENT_PORT"));
        var jvmMetricsPort = Integer.parseInt(System.getProperty("JVM_METRICS_PORT"));
        var jvmMetricsPath = System.getProperty("JVM_METRICS_PATH").toLowerCase();
        Configuration config = new Configuration()
            .metricsEnabled(true)
            .logLevel(logLevel)
            .maxLogEntries(1000)
            .clientNioEventLoopThreadCount(10)
            .actionHandlerThreadCount(5)
            .maximumNumberOfRequestToReturnInVerificationFailure(1)
            .attemptToProxyIfNoMatchingExpectation(false)
            .maxSocketTimeoutInMillis(60000L);
        if (logLevel.equals("ERROR")) {
            config.disableLogging(true) 
                .detailedMatchFailures(false);
        }
        mockServer = ClientAndServer.startClientAndServer(config, clientPort);

        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT);
        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);

        HttpServer server = HttpServer.create(new InetSocketAddress(jvmMetricsPort), 0);
        server.createContext(jvmMetricsPath, httpExchange ->
        {
            String response = prometheusRegistry.scrape();
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            } catch (IOException e) {
                log.error("Error in exposing jvm metrics", e);
            }
        });
        new Thread(server::start).start();
        log.info("JVM metrics are exposing now");
    }
}
