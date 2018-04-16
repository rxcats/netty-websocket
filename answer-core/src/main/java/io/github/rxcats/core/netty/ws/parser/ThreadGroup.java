package io.github.rxcats.core.netty.ws.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ThreadGroup {
    private String groupName;
    private int threadSize;
    private int queueSize;
    private int timeout;
}
