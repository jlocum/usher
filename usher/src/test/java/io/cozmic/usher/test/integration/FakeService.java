package io.cozmic.usher.test.integration;

import io.cozmic.usher.core.*;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

/**
 * Created by chuck on 9/25/14.
 */
public class FakeService extends Verticle {

    public static final int FAKE_SERVICE_PORT = 9191;
    public static final String FAKE_SERVICE_HOST = "localhost";


    public void start(final Future<Void> startedResult)
    {
        final ConcurrentSharedMap<String, Counter> counters = vertx.sharedData().getMap("counters");
        final Counter fakereceiveCounter = counters.get("fakereceive");


        final Integer delay = container.config().getInteger("delay", 1);
        final Buffer fakeTrackingResponse = new Buffer();
        fakeTrackingResponse.appendByte((byte) 0x11);
        fakeTrackingResponse.appendByte((byte) 0x01);

        final NetServer netServer = vertx.createNetServer();

        netServer
                .connectHandler(new Handler<NetSocket>() {
                    @Override
                    public void handle(final NetSocket socket) {
                        socket.exceptionHandler(new Handler<Throwable>() {
                            @Override
                            public void handle(Throwable event) {
                                container.logger().error("Socket error on fake service socket", event);
                            }
                        });
                        final CozmicSocket cozmicSocket = new CozmicSocket(socket, counters.get("purged_fake"));

                        // Pump.createPump(socket, socket).start();
//
//                        ConfigurablePacketSocket configurablePacketSocket = new ConfigurablePacketSocket(socket, counters.get("purged_fake"));
//
//                        configurablePacketSocket.dataHandler(new Handler<Buffer>() {
//                            @Override
//                            public void handle(Buffer event) {
//
//
//                            }
//                        });
//Pump.createPump(configurablePacketSocket.translate(new StreamProcessor() {
//    @Override
//    public void process(Buffer buffer, Handler<AsyncResult<Buffer>> resultHandler) {
//        final int count = fakereceiveCounter.MyCounter.incrementAndGet();
//        if (count % 1000 == 0) {
//            container.logger().info("Fake receive: " + count);
//        }
//        if (count + 1 == 100000) {
//            container.logger().info("Fake receive: " + count);
//        }
////                                   container.logger().info("Fake receive: " + receive);
//        resultHandler.handle(new DefaultFutureResult<Buffer>(fakeTrackingResponse));
//    }
//}), socket).start();
                        final Pump pump = Pump.createPump(cozmicSocket.translate(new CozmicStreamProcessor() {
                            @Override
                            public void process(Message message, Handler<AsyncResult<Message>> resultHandler) {
                                try {
                                    final int count = fakereceiveCounter.MyCounter.incrementAndGet();
//                                    if (count % 1000 == 0) {
//                                        container.logger().info("Fake receive: " + count);
//                                    }
                                    if (count + 1 == 100000) {
                                        container.logger().info("Fake receive: " + count);
                                    }
                                    // container.logger().info("Fake receive: " + count);


                                    final Message reply = message.createReply(fakeTrackingResponse);
                                    resultHandler.handle(new DefaultFutureResult<>(reply));
                                } catch (Exception ex) {
                                    resultHandler.handle(new DefaultFutureResult(ex));
                                }
                            }
                        }), cozmicSocket);
                        pump.start();
//
//                        final CozmicPump cozmicPump = CozmicPump.createPump().start();
//
//                        cozmicPump.


                    }
                })

                .setAcceptBacklog(10000)
                .listen(FAKE_SERVICE_PORT, FAKE_SERVICE_HOST, new Handler<AsyncResult<NetServer>>() {
                    @Override
                    public void handle(AsyncResult<NetServer> event) {
                        if (event.failed()) {
                            container.logger().error(event.cause().getMessage());
                            startedResult.setFailure(event.cause());
                            return;
                        }

                        startedResult.setResult(null);
                    }
                });
    }

}