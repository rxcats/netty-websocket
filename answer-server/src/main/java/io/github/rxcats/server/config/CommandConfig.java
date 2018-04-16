package io.github.rxcats.server.config;

import io.github.rxcats.core.netty.ws.parser.CommandBeanInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class CommandConfig {

    @Autowired ApplicationContext ctx;

    // api 하위 패키지의 @CmdClass, @CmdMethod annotation 으로 command 목록 정리
    @Bean
    public CommandBeanInitializer commandBeanInitializer() {
        return new CommandBeanInitializer(ctx);
    }

    @PostConstruct
    public void initialize() {
        CommandBeanInitializer commandBeanInitializer = ctx.getBean(CommandBeanInitializer.class);
        commandBeanInitializer.initialize();
    }

}
