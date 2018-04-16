package io.github.rxcats.server.message.message;

import lombok.Data;

@Data
public class SendAllReq {
    private String fromPlayer;
    private String message;
}
