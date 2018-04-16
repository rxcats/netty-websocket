package io.github.rxcats.core.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Slf4j
@Sharable
public class JerseyNettyHttpServerHandler extends ChannelInboundHandlerAdapter implements Container {
    private static final AsciiString CONTENT_TYPE = new AsciiString("Content-Type");
    private static final AsciiString CONTENT_LENGTH = new AsciiString("Content-Length");
    private static final AsciiString CONNECTION = new AsciiString("Connection");
    private static final AsciiString KEEP_ALIVE = new AsciiString("keep-alive");

    private static final SecurityContext dummySecurityContext = new DummySecurityContext();

    private final boolean isSecure;
    private final ResourceConfig resourceConfig;

    public JerseyNettyHttpServerHandler(boolean isSecure, ResourceConfig resourceConfig) {
        this.isSecure = isSecure;
        this.resourceConfig = resourceConfig;
    }

    private static void processRequestHeaders(HttpHeaders headers, ContainerRequest requestContext) {
        for (Entry<String, String> header : headers) {
            String value = header.getValue();
            String headerName = header.getKey();
            if (HttpHeaderNames.CONTENT_TYPE.contentEqualsIgnoreCase(headerName) && value.indexOf(';') > 0) {
                value = value.substring(0, value.indexOf(';'));
            }
            requestContext.headers(headerName, value);
        }
    }

    private static void prepareResponseHeaders(ContainerResponse containerResponse, DefaultFullHttpResponse result) {
        MultivaluedMap<String, Object> containerResponseHeaders = containerResponse.getHeaders();
        HttpHeaders responseHeaders = result.headers();
        for (Entry<String, List<Object>> stringListEntry : containerResponseHeaders.entrySet()) {
            String headerName = stringListEntry.getKey();
            List<Object> headerValues = stringListEntry.getValue();
            responseHeaders.add(headerName, headerValues);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("msg:{}", msg);
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            boolean keepAlive = HttpUtil.isKeepAlive(req);

            FullHttpResponse response = consumeRequest(ctx, req);

            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }

    private FullHttpResponse consumeRequest(ChannelHandlerContext ctx, HttpRequest req) {
        FullHttpResponse response;
        ByteBuf buffer = ctx.alloc().buffer();
        try {
            ContainerRequest containerRequest = createContainerRequest(ctx, req);
            ContainerResponse containerResponse = getApplicationHandler()
                .apply(containerRequest, new ByteBufOutputStream(buffer))
                .get();

            response = createNettyResponse(containerResponse, buffer);

        } catch (Exception e) {
            buffer.release();
            log.warn("Can't process the request", e);
            response = new DefaultFullHttpResponse(req.protocolVersion(), INTERNAL_SERVER_ERROR);
            response.headers().set(CONTENT_TYPE, MediaType.TEXT_PLAIN);
        }

        return response;
    }

    private ContainerRequest createContainerRequest(ChannelHandlerContext ctx, HttpRequest req) throws URISyntaxException {
        HttpHeaders headers = req.headers();
        URI baseUri = new URI((isSecure ? "https" : "http") + "://" + headers.get(HttpHeaderNames.HOST) + "/");

        URI requestUri = UriBuilder.fromUri(req.uri())
            .scheme(baseUri.getScheme())
            .host(baseUri.getHost())
            .port(baseUri.getPort())
            .build();

        String httpMethod = req.method().name();

        ContainerRequest requestContext = new ContainerRequest(
            baseUri,
            requestUri,
            httpMethod,
            dummySecurityContext,
            new MapPropertiesDelegate());
        requestContext.setProperty(ContainerRequestHelper.CHANNEL_HANDLER_CONTEXT_PROPERTY, ctx);

        if (req instanceof FullHttpRequest) {
            consumeEntity((FullHttpRequest) req, requestContext);
        }

        processRequestHeaders(headers, requestContext);

        return requestContext;
    }

    private void consumeEntity(FullHttpRequest req, ContainerRequest requestContext) {
        ByteBuf content = req.content();
        if (content != null) {
            requestContext.setEntityStream(new ByteBufInputStream(content));
        }
    }

    private FullHttpResponse createNettyResponse(ContainerResponse containerResponse, ByteBuf buffer) {
        HttpResponseStatus status = HttpResponseStatus.valueOf(containerResponse.getStatus());

        DefaultFullHttpResponse result = new DefaultFullHttpResponse(HTTP_1_1, status, buffer, true);

        prepareResponseHeaders(containerResponse, result);

        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        return new ApplicationHandler(resourceConfig);
    }

    @Override
    public ResourceConfig getConfiguration() {
        return resourceConfig;
    }

    @Override
    public void reload() {
        reload(getConfiguration());
    }

    @Override
    public void reload(ResourceConfig configuration) {
        throw new UnsupportedOperationException();
    }
}