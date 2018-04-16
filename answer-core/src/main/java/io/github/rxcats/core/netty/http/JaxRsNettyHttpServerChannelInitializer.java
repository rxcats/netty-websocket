package io.github.rxcats.core.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.glassfish.jersey.server.ResourceConfig;

public class JaxRsNettyHttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ResourceConfig resourceConfig;
    private final boolean isSecure;

    public JaxRsNettyHttpServerChannelInitializer(boolean isSecure, ResourceConfig resourceConfig) {
        this.isSecure = isSecure;
        this.resourceConfig = resourceConfig;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().
            addLast(new HttpServerCodec())
            .addLast(new HttpObjectAggregator(1048576))
            .addLast(new HttpContentCompressor())
            .addLast(new JerseyNettyHttpServerHandler(isSecure, resourceConfig));
    }
}
