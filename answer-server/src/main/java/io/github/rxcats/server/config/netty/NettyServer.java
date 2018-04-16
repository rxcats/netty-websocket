package io.github.rxcats.server.config.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rxcats.core.netty.http.JaxRsNettyHttpServer;
import io.github.rxcats.core.netty.http.JaxRsNettyHttpServerImpl;
import io.github.rxcats.core.netty.http.NettyHttpServerProperties;
import io.github.rxcats.core.netty.ws.NettyWebSocketServer;
import io.github.rxcats.core.netty.ws.NettyWebSocketServerHandler;
import io.github.rxcats.core.netty.ws.NettyWebSocketServerImpl;
import io.github.rxcats.core.netty.ws.NettyWebSocketServerProperties;
import io.github.rxcats.core.netty.ws.parser.PacketParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyServer {
    @Bean
    public PacketParser packetParser(ObjectMapper objectMapper) {
        return new PacketParser(objectMapper);
    }

    @Bean
    public NettyWebSocketServerHandler nettyWebSocketServerHandler(PacketParser packetParser) {
        return new NettyWebSocketServerHandler(packetParser);
    }

    @Bean
    public NettyWebSocketServer nettyWebSocketServer(NettyWebSocketServerHandler nettyWebSocketServerHandler,
                                                     NettyWebSocketServerConfig nettyWebSocketServerConfig) {

        NettyWebSocketServerProperties properties = NettyWebSocketServerProperties.builder()
            .port(nettyWebSocketServerConfig.getPort())
            .path(nettyWebSocketServerConfig.getPath())
            .subProtocol(nettyWebSocketServerConfig.getSubProtocol())
            .maxContentLength(nettyWebSocketServerConfig.getMaxContentLength())
            .bootstrapLogLevel(nettyWebSocketServerConfig.getBootstrapLogLevel())
            .pipelineLogLevel(nettyWebSocketServerConfig.getPipelineLogLevel())
            .allowExtensions(nettyWebSocketServerConfig.isAllowExtensions())
            .build();

        NettyWebSocketServer webSocketServer = new NettyWebSocketServerImpl(nettyWebSocketServerHandler, properties);
        webSocketServer.initialize();
        return webSocketServer;
    }

    @Bean
    public JaxRsNettyHttpServer jaxRsNettyHttpServer(ApplicationContext ctx, NettyHttpServerConfig nettyHttpServerConfig) {
        NettyHttpServerProperties properties = NettyHttpServerProperties.builder()
            .port(nettyHttpServerConfig.getPort())
            .maxContentLength(nettyHttpServerConfig.getMaxContentLength())
            .bootstrapLogLevel(nettyHttpServerConfig.getBootstrapLogLevel())
            .pipelineLogLevel(nettyHttpServerConfig.getPipelineLogLevel())
            .build();

        JaxRsNettyHttpServer httpServer = new JaxRsNettyHttpServerImpl(ctx, properties);
        httpServer.initialize();
        return httpServer;
    }
}
