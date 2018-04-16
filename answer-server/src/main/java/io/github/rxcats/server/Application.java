package io.github.rxcats.server;

import io.github.rxcats.core.netty.http.JaxRsNettyHttpServer;
import io.github.rxcats.core.netty.ws.NettyWebSocketServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        NettyWebSocketServer nettyWebSocketServer = context.getBean(NettyWebSocketServer.class);
        nettyWebSocketServer.start();

        JaxRsNettyHttpServer http = context.getBean(JaxRsNettyHttpServer.class);
        http.start();
    }
}
