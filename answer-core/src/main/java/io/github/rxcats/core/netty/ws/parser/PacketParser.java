package io.github.rxcats.core.netty.ws.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rxcats.core.command.ApiCommand;
import io.github.rxcats.core.constant.CmdResultCode;
import io.github.rxcats.core.message.ErrorEntity;
import io.github.rxcats.core.message.WsResponseEntity;
import io.github.rxcats.core.netty.ws.annotation.CmdParamBody;
import io.github.rxcats.core.netty.ws.annotation.CmdSession;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class PacketParser {
    private ObjectMapper mapper;

    public PacketParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    // packet : {"method":"", "params":""}
    public String execute(Channel channel, String request) {
        log.info("json request:{}", request);

        String method = "";
        String params;

        Object result = null;
        WsResponseEntity<Object> response;
        try {
            JSONObject j = new JSONObject(request);
            method = j.get("method").toString();
            params = j.get("params").toString();

            String[] parsedMethod = parseMethod(method);
            log.info("parsed method array:{}", Arrays.asList(parsedMethod));

            if (parsedMethod.length < 2) {
                throw new IllegalArgumentException("Could not find command");
            }

            String className = parsedMethod[1];
            String methodName = parsedMethod[2];
            CommandBeanHolder holder = CommandBeanInitializer.getCommandBean("/" + className);
            List<Annotation> paramAnnotationList = holder.getMethodParamAnnotationList().get("/" + methodName);
            Method m = holder.getMethodMap().get("/" + methodName);
            Object[] p = invokeParams(m, paramAnnotationList, params, channel);

            // api 클래스별로 command 를 이용하도록
            ApiCommand cmd = new ApiCommand(this, className, methodName, holder.getThreadGroup().getGroupName(),
                holder.getThreadGroup().getThreadSize(), holder.getThreadGroup().getQueueSize(), holder.getThreadGroup().getTimeout(),
                m, holder.getClazz(), p);
            result = cmd.execute();
            response = new WsResponseEntity<>(method, result);
        } catch (Exception e) {
            log.warn("execute command error : {}", e.getMessage());
            response = new WsResponseEntity<>(method, new ErrorEntity(e.getMessage()), CmdResultCode.INTERNAL_SERVER_ERROR.getCode());
        }

        log.info("response object:{}", result);

        return toJson(response);
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("toJson error : {}", e.getMessage());
            return "";
        }
    }

    private Object fromJson(String json, Class<?> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            log.warn("fromJson error : {}", e.getMessage());
            return null;
        }
    }

    private String[] parseMethod(String methodName) {
        if (StringUtils.isEmpty(methodName)) {
            throw new IllegalArgumentException("Unable execute method, method name required");
        }
        return methodName.split("/", 3);
    }

    private Object[] invokeParams(Method method, List<Annotation> paramAnnotationList, String params, Channel channel) {
        List<Object> paramValue = new ArrayList<>();

        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramAnnotationList.size() == paramTypes.length && paramAnnotationList.size() > 0) {
            for (int i = 0; i < paramAnnotationList.size(); i++) {
                Annotation annotation = paramAnnotationList.get(i);

                // WebSocket Session 정보 파라미터의 경우 Type 이 Channel 인지 확인
                if (annotation instanceof CmdSession && paramTypes[i].equals(Channel.class)) {
                    paramValue.add(i, channel);
                } else if (annotation instanceof CmdParamBody) { // Json Request Body
                    paramValue.add(i, fromJson(params, paramTypes[i]));
                }
            }
        }

        log.info("api class bind param value:{}", paramValue);

        return paramValue.toArray();
    }

    public TextWebSocketFrame convertWebSocketFrame(String response) {
        return new TextWebSocketFrame(response);
    }

    public String errorResponse(String error) {
        return toJson(new WsResponseEntity<>("/exception", new ErrorEntity(error), CmdResultCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    public String convertResponse(String method, Object result) {
        return toJson(new WsResponseEntity<>(method, result));
    }

}
