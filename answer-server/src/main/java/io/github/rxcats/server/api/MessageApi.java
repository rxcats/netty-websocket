package io.github.rxcats.server.api;

import io.github.rxcats.core.message.EmptyResponseBody;
import io.github.rxcats.core.netty.ws.annotation.CmdClass;
import io.github.rxcats.core.netty.ws.annotation.CmdMethod;
import io.github.rxcats.core.netty.ws.annotation.CmdParamBody;
import io.github.rxcats.server.message.message.SendAllReq;
import io.github.rxcats.server.message.message.SendReq;
import io.github.rxcats.server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;

@CmdClass(prefix = "/message", desc = "message api", groupName = "common", threadSize = 5, queueSize = 50, timeout = 3000)
public class MessageApi {

    @Autowired
    private MessageService messageService;

    // example : {"method":"/message/sendAll","params":{"fromPlayer":"p1","message":"hello everyone"}}
    @CmdMethod(name = "/sendAll", desc = "메시지 전체 발송")
    public EmptyResponseBody sendAll(@CmdParamBody SendAllReq request) {
        messageService.sendBroadcast("/message/sendAll", request.getFromPlayer(), request.getMessage());
        return new EmptyResponseBody();
    }

    // example :  {"method":"/message/send","params":{"player":"p1","targetPlayer":"p2","message":"hello"}}
    @CmdMethod(name = "/send", desc = "특정 유저에게 메시지 발송")
    public EmptyResponseBody send(@CmdParamBody SendReq request) {
        messageService.sendMessage("/message/send", request.getPlayer(), request.getTargetPlayer(), request.getMessage());
        return new EmptyResponseBody();
    }
}
