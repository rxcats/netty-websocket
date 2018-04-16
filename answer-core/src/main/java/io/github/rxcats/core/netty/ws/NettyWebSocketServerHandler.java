package io.github.rxcats.core.netty.ws;

import io.github.rxcats.core.netty.ws.parser.PacketParser;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private PacketParser packetParser;

    public NettyWebSocketServerHandler(PacketParser packetParser) {
        this.packetParser = packetParser;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("Unsupported frame type : %s", frame.getClass().getName()));
        }
        log.info("WebSocketFrame context : {}", frame);

        final String frameText = ((TextWebSocketFrame) frame).text();
        ctx.writeAndFlush(packetParser.convertWebSocketFrame(packetParser.execute(ctx.channel(), frameText)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        log.info("channelInactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.writeAndFlush(packetParser.convertWebSocketFrame(packetParser.errorResponse(cause.getMessage())));
        super.exceptionCaught(ctx, cause);
    }
}
