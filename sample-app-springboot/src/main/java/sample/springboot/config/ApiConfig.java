package sample.springboot.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.github.xerprojects.xerj.commandstack.CommandDispatcher;
import io.github.xerprojects.xerj.commandstack.CommandHandlerProvider;
import io.github.xerprojects.xerj.commandstack.dispatchers.CommandStackDispatcher;
import io.github.xerprojects.xerj.commandstack.dispatchers.async.AsyncCommandDispatcher;
import sample.springboot.Application;
import sample.springboot.providers.SpringContextCommandHandlerProvider;

@Configuration
@ComponentScan(basePackageClasses = Application.class)
public class ApiConfig {

    private static final Logger DISPATCHER_LOGGER = LoggerFactory.getLogger(CommandStackDispatcher.class);
	private static final ObjectMapper OBJECT_MAPPER = 
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Bean
    @Primary
    public CommandDispatcher commandDispatcher(
            CommandStackDispatcher commandStackDispatcher,
            ThreadPoolTaskExecutor taskExecutor) {
        return new AsyncCommandDispatcher(commandStackDispatcher,
            taskExecutor.getThreadPoolExecutor());
    }

    @Bean
    public CommandStackDispatcher commandStackDispatcher(
            CommandHandlerProvider commandHandlerProvider) {
        return new CommandStackDispatcher(commandHandlerProvider,
            this::logUnhandledCommand);
    }

    @Bean
    public CommandHandlerProvider commandHandlerProvider(ApplicationContext appContext) {
        return new SpringContextCommandHandlerProvider(appContext);
    }

    private <TCommand> void logUnhandledCommand(TCommand command) {
        DISPATCHER_LOGGER.warn("No command handler found for command of type {}.", command.getClass());
        try {
			DISPATCHER_LOGGER.warn("Unhandled command:{}{}",
                System.lineSeparator(),
                OBJECT_MAPPER.writeValueAsString(command));
		} catch (JsonProcessingException e) {
			DISPATCHER_LOGGER.warn("Unexpected exception occurred while trying to serialize command.", e);
		}
    }
}
