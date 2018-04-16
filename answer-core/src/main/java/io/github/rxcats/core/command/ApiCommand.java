package io.github.rxcats.core.command;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import io.github.rxcats.core.netty.ws.parser.PacketParser;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class ApiCommand extends HystrixCommand<Object> {

    private final PacketParser parser;
    private final Method m;
    private final Object clazz;
    private final Object[] params;

    public ApiCommand(PacketParser parser, String groupKey, String commandKey, String threadPoolKey, int threadSize,
                      int queueSize, int timeout, Method m, Object clazz, Object[] params) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
            .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
            .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(threadPoolKey))
            .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withMaximumSize(threadSize).withMaxQueueSize(queueSize))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeout))
        );

        this.parser = parser;
        this.m = m;
        this.clazz = clazz;
        this.params = params;
    }

    @Override
    protected Object run() throws Exception {
        log.info("HystrixCommand run:{}", m.toString());
        return m.invoke(clazz, params);
    }

    @Override
    protected Object getFallback() {
        return parser.errorResponse("Unable execute command: " + m.toString());
    }
}
