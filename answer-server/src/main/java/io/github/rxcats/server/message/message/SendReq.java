package io.github.rxcats.server.message.message;

import lombok.Data;

@Data
public class SendReq {
    private String player;
    private String targetPlayer;
    private String message;
}
