package io.cozmic.usher.streams;

import io.cozmic.usher.message.PipelinePack;
import io.vertx.core.Handler;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

/**
 * Created by chuck on 6/30/15.
 */
public class DuplexStream<R, W> {
    private final ReadStream<R> readStream;
    private final WriteStream<W> writeStream;
    private Handler<PipelinePack> packDecorator;

    public DuplexStream(ReadStream<R> readStream, WriteStream<W> writeStream) {
        this.readStream = readStream;
        this.writeStream = writeStream;
    }

    public DuplexStream(ReadStream<R> readStream, WriteStream<W> writeStream, Handler<PipelinePack> packDecorator) {
        this(readStream, writeStream);
        this.packDecorator = packDecorator;
    }

    public WriteStream<W> getWriteStream() {
        return writeStream;
    }

    public ReadStream<R> getReadStream() {
        return readStream;
    }

    public ReadStream<R> pause() {
        return getReadStream().pause();
    }

    public ReadStream<R> resume() {
        return getReadStream().resume();
    }


    public void decorate(PipelinePack pack, Handler<PipelinePack> decoratedHandler) {
        if (packDecorator == null) {
            decoratedHandler.handle(pack);
            return;
        }

        packDecorator.handle(pack);
        decoratedHandler.handle(pack);
    }
}
