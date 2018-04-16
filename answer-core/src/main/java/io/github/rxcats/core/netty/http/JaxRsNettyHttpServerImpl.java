package io.github.rxcats.core.netty.http;

import io.github.rxcats.core.netty.http.annotation.EndPoint;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toList;

@Slf4j
public class JaxRsNettyHttpServerImpl implements JaxRsNettyHttpServer {

    private ApplicationContext applicationContext;
    private NettyHttpServerProperties properties;

    private Channel channel;
    private ServerBootstrap serverBootstrap;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    public JaxRsNettyHttpServerImpl(ApplicationContext applicationContext, NettyHttpServerProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    private List<Class> getEndPointClasses() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EndPoint.class);
        List<Class> classes = new ArrayList<>();
        if (!CollectionUtils.isEmpty(beans)) {
            classes.addAll(
                beans.entrySet().stream()
                    .map(Entry::getKey)
                    .map(name -> applicationContext.getBean(name))
                    .map(Object::getClass)
                    .collect(toList())
            );
        }
        return classes;
    }

    @Override
    public void initialize() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(JacksonFeature.class);
        resourceConfig.register(LoggingFeature.class);
        resourceConfig.register(MultiPartFeature.class);

        // endpoint classes
        List<Class> classes = getEndPointClasses();
        log.info("endpoint class size:{}", classes.size());
        for (Class clazz : classes) {
            log.info("endpoint class : {}", clazz);
            resourceConfig.register(clazz);
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

        serverBootstrap
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(properties.getBootstrapLogLevel()))
            .childHandler(new JaxRsNettyHttpServerChannelInitializer(false, resourceConfig));
    }

    @Override
    public void start() {
        try {
            channel = serverBootstrap.bind(properties.getPort()).sync().channel();
            log.info("Jaxrs Http Server started. 0.0.0.0:{}", properties.getPort());
        } catch (InterruptedException e) {
            log.warn("Shutting down Http Server...");
            stop();
        }
    }

    @Override
    public void stop() {
        if (channel != null) {
            channel.close();
            channel.parent().close();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
