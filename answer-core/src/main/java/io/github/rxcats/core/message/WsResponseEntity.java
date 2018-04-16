package io.github.rxcats.core.message;

import io.github.rxcats.core.constant.CmdResultCode;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Data
public class WsResponseEntity<T> implements Serializable {
    private static final long serialVersionUID = 5834976577752458668L;

    private String method;
    private int status;
    private T body;
    private long ts;
    private String date;

    public WsResponseEntity() {
        this.method = "";
        this.status = CmdResultCode.OK.getCode();

        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime zdt = ldt.atZone(ZoneOffset.UTC);

        this.ts = zdt.toEpochSecond();
        this.date = zdt.toString();
    }

    public WsResponseEntity(final String method, final T body) {
        this();
        this.method = method;
        this.body = body;
    }

    public WsResponseEntity(final String method, final T body, final int status) {
        this();
        this.method = method;
        this.body = body;
        this.status = status;
    }
}
