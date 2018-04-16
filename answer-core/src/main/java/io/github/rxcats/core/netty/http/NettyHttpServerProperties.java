package io.github.rxcats.core.netty.http;

import io.netty.handler.logging.LogLevel;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NettyHttpServerProperties {
    private int port;
    private int maxContentLength;
    private LogLevel pipelineLogLevel;
    private LogLevel bootstrapLogLevel;
}
