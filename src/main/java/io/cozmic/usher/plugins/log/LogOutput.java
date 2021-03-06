package io.cozmic.usher.plugins.log;

import io.cozmic.usher.core.OutPipeline;
import io.cozmic.usher.core.OutputPlugin;
import io.cozmic.usher.message.Message;
import io.cozmic.usher.streams.DuplexStream;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

/**
 * Created by chuck on 8/4/15.
 */
public class LogOutput implements OutputPlugin {
    private JsonObject configObj;
    private Vertx vertx;

    @Override
    public void run(AsyncResultHandler<DuplexStream<Buffer, Buffer>> duplexStreamAsyncResultHandler) {
        LogSinkStream logStream = new LogSinkStream();
        duplexStreamAsyncResultHandler.handle(Future.succeededFuture(new DuplexStream<>(logStream, logStream, pack -> {
            final Message message = pack.getMessage();

        })));
    }

    @Override
    public void stop(OutPipeline outPipeline) {

    }

    @Override
    public void init(JsonObject configObj, Vertx vertx) {

        this.configObj = configObj;
        this.vertx = vertx;
    }

    private class LogSinkStream implements ReadStream<Buffer>, WriteStream<Buffer> {

        private Logger logger;
        private Handler<Buffer> dataHandler;

        public LogSinkStream() {
            logger = LoggerFactory.getLogger(configObj.getString("name", "default_log"));
        }

        @Override
        public LogSinkStream exceptionHandler(Handler<Throwable> handler) {
            return this;
        }

        @Override
        public WriteStream<Buffer> write(Buffer data) {
            logger.info(data.toString());
            if (configObj.getBoolean("successResponse", false)) {
                dataHandler.handle(Buffer.buffer(new byte[] {0x1}));
            }
            return this;
        }

        @Override
        public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
            return this;
        }

        @Override
        public boolean writeQueueFull() {
            return false;
        }

        @Override
        public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
            return this;
        }

        @Override
        public ReadStream<Buffer> handler(Handler<Buffer> dataHandler) {
            this.dataHandler = dataHandler;
            return this;
        }

        @Override
        public ReadStream<Buffer> pause() {
            return this;
        }

        @Override
        public ReadStream<Buffer> resume() {
            return this;
        }

        @Override
        public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
            return this;
        }
    }
}
