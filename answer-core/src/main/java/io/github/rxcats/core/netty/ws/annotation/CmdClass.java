package io.github.rxcats.core.netty.ws.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CmdClass {
    String prefix() default "";

    String desc() default "";

    String groupName() default "";

    int threadSize() default 0;

    int queueSize() default 0;

    int timeout() default 0;
}
