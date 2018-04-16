package io.github.rxcats.core.netty.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyWebSocketServerImpl implements NettyWebSocketServer {
    private NettyWebSocketServerHandler nettyWebSocketServerHandler;
    private NettyWebSocketServerProperties properties;

    private Channel channel;
    private ServerBootstrap serverBootstrap;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    public NettyWebSocketServerImpl(NettyWebSocketServerHandler nettyWebSocketServerHandler, NettyWebSocketServerProperties properties) {
        this.nettyWebSocketServerHandler = nettyWebSocketServerHandler;
        this.properties = properties;
    }

    @Override
    public void initialize() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        final ChannelInitializer<SocketChannel> nettyChannelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel channel) throws Exception {
                channel.pipeline()
                    .addLast(new HttpServerCodec())
                    .addLast(new HttpObjectAggregator(properties.getMaxContentLength()))
                    .addLast(new WebSocketServerCompressionHandler())
                    .addLast(new WebSocketServerProtocolHandler(properties.getPath(), properties.getSubProtocol(), properties.isAllowExtensions()))
                    .addLast(new LoggingHandler(properties.getPipelineLogLevel()))
                    //.addLast(new ReadTimeoutHandler(30))
                    .addLast(nettyWebSocketServerHandler);
            }
        };

        serverBootstrap = new ServerBootstrap();

        serverBootstrap
            .group(bossGroup, workerGroup)
            .handler(new LoggingHandler(properties.getBootstrapLogLevel()))
            .childHandler(nettyChannelInitializer)
            .channel(NioServerSocketChannel.class);
    }

    @Override
    public void start() {
        try {
            channel = serverBootstrap.bind(properties.getPort()).sync().channel();
            log.info("WebSocket Server started. 0.0.0.0:{}", properties.getPort());
        } catch (InterruptedException e) {
            log.warn("Shutting down WebSocket Server...");
            stop();
        }
    }

    @Override
    public void stop() {
        if (channel != null) {
            channel.close();
            channel.parent().close();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }
}
