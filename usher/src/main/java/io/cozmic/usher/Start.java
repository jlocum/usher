package io.cozmic.usher;


import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import org.crsh.vfs.Path;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.file.impl.PathAdjuster;
import org.vertx.java.core.impl.DefaultVertx;
import org.vertx.java.core.impl.VertxInternal;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.io.File;
import java.sql.Time;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chuck on 10/23/14.
 */
public class Start extends Verticle {
    public static final MetricRegistry metrics = new MetricRegistry();
    public void start(final Future<Void> startedResult) {

        container.logger().info("Config: " + container.config().toString());
        final Handler<AsyncResult<String>> doneHandler = prerequisitesReadyHandler(startedResult, 4);

        final int instanceCountForRocksVerticles = 1; //Only one thread can access the rocksDB

        container.deployWorkerVerticle(Journaler.class.getName(), container.config().getObject("journaler_config"), instanceCountForRocksVerticles, true, doneHandler);
        container.deployWorkerVerticle(TimeoutLogger.class.getName(), container.config().getObject("timeout_logger_config"), instanceCountForRocksVerticles, true, doneHandler);
        container.deployVerticle(EchoChamber.class.getName(), doneHandler);
        final JsonObject shellConfig = container.config().getObject("shell_config");
        if (shellConfig != null) {
            final String commandDir = PathAdjuster.adjust((VertxInternal) vertx, "./commands");
            container.logger().info("Setting custom crash command directory to " + commandDir);
            shellConfig.putString("cmd", commandDir);
        }
        container.deployModule("org.crashub~vertx.shell~2.1.0", shellConfig, doneHandler);

        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(3, TimeUnit.SECONDS);
    }


    protected Handler<AsyncResult<String>> prerequisitesReadyHandler(final Future<Void> startedResult, final int numberOfPrerequisites) {
        final AtomicInteger prereqsLaunched = new AtomicInteger();
        return new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                if (asyncResult.failed()) {
                    final Throwable cause = asyncResult.cause();
                    container.logger().error(cause.getMessage(), cause);
                    startedResult.setFailure(cause);
                    stop();
                    return;
                }

                final int launchedSoFar = prereqsLaunched.incrementAndGet();
                container.logger().info("Deployed prereq");
                if (launchedSoFar == numberOfPrerequisites) {
                    final int proxyInstances = Runtime.getRuntime().availableProcessors() / 2;
                    container.deployVerticle(Proxy.class.getName(), container.config().getObject("proxy_config"), proxyInstances, new Handler<AsyncResult<String>>() {
                        @Override
                        public void handle(AsyncResult<String> asyncResult) {
                            if (asyncResult.failed()) {
                                container.logger().error(asyncResult.cause());
                                startedResult.setFailure(asyncResult.cause());
                                stop();
                                return;
                            }

                            container.logger().info("Proxy deployed");
                            startedResult.setResult(null);
                        }
                    });
                }
            }
        };
    }

}

