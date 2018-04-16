package io.github.rxcats.core.netty.ws.parser;

import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CommandBeanHolder {
    private String prefix;
    private Object clazz;
    private ThreadGroup threadGroup;
    private Map<String, Method> methodMap;
    private Map<String, List<Annotation>> methodParamAnnotationList;

    public CommandBeanHolder() {
        methodMap = new HashMap<>();
        methodParamAnnotationList = new HashMap<>();
    }
}
