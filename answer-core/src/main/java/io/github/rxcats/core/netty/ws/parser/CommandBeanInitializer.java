package io.github.rxcats.core.netty.ws.parser;

import io.github.rxcats.core.netty.ws.annotation.CmdClass;
import io.github.rxcats.core.netty.ws.annotation.CmdMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Slf4j
public class CommandBeanInitializer {
    private static final Map<String, CommandBeanHolder> commandBeanHolderMap = new HashMap<>();

    private ApplicationContext applicationContext;

    public CommandBeanInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static CommandBeanHolder getCommandBean(final String name) {
        return commandBeanHolderMap.get(name);
    }

    public void initialize() {
        final Map<String, Object> list = applicationContext.getBeansWithAnnotation(CmdClass.class);

        list.forEach((k, v) -> {
            Object bean = applicationContext.getBean(k);
            String prefix = bean.getClass().getAnnotation(CmdClass.class).prefix();
            if (prefix.equals("")) {
                prefix = bean.getClass().getSimpleName();
            }

            String groupName = bean.getClass().getAnnotation(CmdClass.class).groupName();
            int threadSize = bean.getClass().getAnnotation(CmdClass.class).threadSize();
            int queueSize = bean.getClass().getAnnotation(CmdClass.class).queueSize();
            int timeout = bean.getClass().getAnnotation(CmdClass.class).timeout();

            Method[] methods = bean.getClass().getMethods();

            CommandBeanHolder holder = new CommandBeanHolder();

            holder.setClazz(bean);
            holder.setPrefix(prefix);
            holder.setThreadGroup(new ThreadGroup(groupName, threadSize, queueSize, timeout));

            for (final Method m : methods) {
                CmdMethod cmdMethod = m.getDeclaredAnnotation(CmdMethod.class);

                if (cmdMethod != null) {
                    String methodName = m.getName();
                    String annotationName = null;
                    String annotationDesc = null;
                    if (!cmdMethod.name().equals("")) {
                        annotationName = cmdMethod.name();
                        annotationDesc = cmdMethod.desc();
                    }

                    // CmdMethod 파라미터의 애노테이션
                    List<Annotation> paramAnnotationList = Arrays.stream(m.getParameterAnnotations())
                        .filter(Objects::nonNull)
                        .flatMap(Arrays::stream)
                        .collect(toList());

                    holder.getMethodMap().put(annotationName, m);
                    holder.getMethodParamAnnotationList().put(annotationName, paramAnnotationList);
                    commandBeanHolderMap.put(prefix, holder);
                    log.info("json api bean prefix:{}, methodName:{} annotationDesc:{} commandBeanHolderMap:{} ", prefix, methodName, annotationDesc, commandBeanHolderMap.get(prefix));
                }
            }
        });

        commandBeanHolderMap.forEach((k, v) ->
            log.info("registered json api :{} {}", k, v)
        );
    }

}
