package io.cozmic.usherprotocols.core;

import org.vertx.java.core.AsyncResultHandler;

/**
 * Created by chuck on 9/30/14.
 */
public interface CozmicStreamProcessor {
    void process(Message message, AsyncResultHandler<Message> replyHandler);
}
