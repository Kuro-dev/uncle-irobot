package org.kurodev.discord.message;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.jetbrains.annotations.NotNull;
import org.kurodev.discord.message.command.AutoRegister;
import org.kurodev.discord.message.command.Command;
import org.kurodev.discord.message.command.generic.HelpCommand;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kuro
 **/
public class CommandHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<Command> commands = new ArrayList<>();


    public List<Command> getCommands() {
        return commands;
    }

    /**
     * Instantiates all Commands that need additional Context and do not have a default Constructor.
     */
    public void prepare() {
        logger.info("initializing commands");
        commands.add(new HelpCommand(commands));
        loadAutoRegisteredCommands();
        logger.info("Loaded a total of {} commands", commands.size());
        initialize();
    }

    /**
     * Uses Reflection magic to automatically load up Commands with {@link AutoRegister} annotation
     */
    private void loadAutoRegisteredCommands() {
        int autowired = 0;
        logger.info("Loading autowired commands");
        Reflections reflections = new Reflections("org.kurodev.discord.message.command");
        var commands = reflections.getSubTypesOf(Command.class);
        for (Class<? extends Command> com : commands) {
            if (com.isAnnotationPresent(AutoRegister.class)) {
                if (com.getAnnotationsByType(AutoRegister.class)[0].load())
                    for (Constructor<?> constructor : com.getConstructors()) {
                        if (constructor.getParameterCount() == 0) {
                            try {
                                Command obj = (Command) constructor.newInstance();
                                boolean anyMatch = this.commands.stream().anyMatch(command -> command.getClass() == obj.getClass());
                                if (!anyMatch) {
                                    this.commands.add(obj);
                                    autowired++;
                                } else {
                                    logger.info("autowired command {} was already registered.", obj);
                                }
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                logger.error("Failed to instantiate \"" + com + "\"", e);
                            }
                        }
                    }
            }
        }
        logger.info("Autowired {} commands", autowired);
    }

    private void initialize() {
        logger.info("Initializing Commands");
        for (Command command : commands) {
            command.prepare();
        }
        List<Command> failed = commands.stream()
                .filter(command -> command.getState() == State.OFFLINE)
                .collect(Collectors.toList());
        logger.info("Successfully initialized {} commands", commands.size() - failed.size());
        if (!failed.isEmpty()) {
            logger.warn("Failed to initialize the following commands:");
            for (Command command : failed) {
                logger.warn(command.getCommand());
            }
        }
    }

    public void handle(String command, MessageReceivedEvent event, String[] strArgs) {
        MessageChannel channel = event.getChannel();
        channel.sendTyping().complete();
        for (Command com : commands) {
            if (com.check(command, event)) {
                CommandLineParser parser = new DefaultParser();
                try {
                    CommandLine args = parser.parse(com.getOptions(), strArgs, true);
                    com.execute(channel, args, event);
                } catch (Throwable e) {
                    channel.sendMessage(e.getMessage()).queue();
                    logger.error("something went wrong in command {}", com.getCommand(), e);
                }
                return;
            }
        }
        channel.sendMessage("Command is unknown, try using !k help").queue();
        event.getMessage().addReaction("🤷‍♂️").queue();
    }


    public void onShutDown() {
        for (Command command : commands) {
            try {
                command.onShutdown();
            } catch (Exception e) {
                logger.error("Failed to execute shutdown for {}", command);
            }
        }
    }
}
