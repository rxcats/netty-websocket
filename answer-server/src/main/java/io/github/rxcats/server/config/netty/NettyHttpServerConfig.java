package io.github.rxcats.server.config.netty;

import io.netty.handler.logging.LogLevel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "netty.server.http")
public class NettyHttpServerConfig {
	private int port;
	private int maxContentLength;
	private LogLevel pipelineLogLevel;
	private LogLevel bootstrapLogLevel;
}
