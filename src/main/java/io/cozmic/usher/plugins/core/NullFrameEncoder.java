package io.cozmic.usher.plugins.core;

import io.cozmic.usher.core.FrameEncoderPlugin;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * Created by chuck on 7/10/15.
 */
public class NullFrameEncoder implements FrameEncoderPlugin{
    private Handler<Buffer> writeHandler;
    private JsonObject configObj;
    private Vertx vertx;

    @Override
    public void encodeAndWrite(Buffer buffer) {
        writeHandler.handle(buffer);
    }

    @Override
    public void setWriteHandler(Handler<Buffer> writeHandler) {

        this.writeHandler = writeHandler;
    }

    @Override
    public FrameEncoderPlugin createNew() {
        final NullFrameEncoder nullFrameEncoder = new NullFrameEncoder();
        nullFrameEncoder.init(configObj, vertx);
        return nullFrameEncoder;
    }

    @Override
    public void init(JsonObject configObj, Vertx vertx) {

        this.configObj = configObj;
        this.vertx = vertx;
    }
}
