package io.github.rxcats.core.netty.http;

import io.netty.channel.ChannelHandlerContext;
import org.glassfish.jersey.server.ContainerRequest;

public class ContainerRequestHelper {
    public static final String CHANNEL_HANDLER_CONTEXT_PROPERTY = ChannelHandlerContext.class.getName();

    private ContainerRequestHelper() {

    }

    public static ChannelHandlerContext getChannelHandlerContext(ContainerRequest containerRequest) {
        return (ChannelHandlerContext) containerRequest.getProperty(CHANNEL_HANDLER_CONTEXT_PROPERTY);
    }
}
