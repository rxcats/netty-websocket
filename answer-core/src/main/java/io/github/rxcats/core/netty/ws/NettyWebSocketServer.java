package io.github.rxcats.core.netty.ws;

public interface NettyWebSocketServer {
    void initialize();

    void start();

    void stop();
}
