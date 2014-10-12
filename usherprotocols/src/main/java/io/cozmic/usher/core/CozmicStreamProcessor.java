package io.cozmic.usher.core;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;

/**
 * Created by chuck on 9/30/14.
 */
public interface CozmicStreamProcessor {
    void process(Message message, Handler<AsyncResult<Message>> resultHandler);
}
