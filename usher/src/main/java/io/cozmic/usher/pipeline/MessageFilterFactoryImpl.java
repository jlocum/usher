package io.cozmic.usher.pipeline;

import io.cozmic.usher.core.EncoderPlugin;
import io.cozmic.usher.core.MessageFilter;
import io.cozmic.usher.core.MessageFilterFactory;
import io.cozmic.usher.plugins.core.NullEncoder;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

/**
 * Created by chuck on 6/30/15.
 */
public class MessageFilterFactoryImpl implements MessageFilterFactory {


    private final PluginIndex<EncoderPlugin> encoderIndex;

    public MessageFilterFactoryImpl(PluginLoader pluginLoader) {
        encoderIndex = pluginLoader.getEncoderIndex();
    }



    @Override
    public MessageFilter createFilter(String pluginName, WriteStream<Buffer> writeStream){
        EncoderPlugin encoderPlugin = createEncoder(pluginName);
        return new MessageFilterImpl(writeStream, encoderPlugin);
    }

    private EncoderPlugin createEncoder(String pluginName) {
        if (!encoderIndex.exists(pluginName)) {
            return new NullEncoder();
        }

        final EncoderPlugin decoderPlugin = encoderIndex.get(pluginName);
        return decoderPlugin.createNew();
    }
}