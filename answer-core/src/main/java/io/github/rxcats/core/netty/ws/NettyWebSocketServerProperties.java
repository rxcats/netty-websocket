package io.github.rxcats.core.netty.ws;

import io.netty.handler.logging.LogLevel;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NettyWebSocketServerProperties {
    private int port;
    private int maxContentLength;
    private String path;
    private String subProtocol;
    private boolean allowExtensions;
    private LogLevel pipelineLogLevel;
    private LogLevel bootstrapLogLevel;
}
