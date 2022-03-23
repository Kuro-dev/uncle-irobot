package org.kurodev.discord.message;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.kurodev.Main;
import org.kurodev.config.Setting;
import org.kurodev.discord.DiscordBot;
import org.kurodev.discord.message.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author kuro
 **/
public class MessageEventHandler extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CommandHandler commandHandler = new CommandHandler();
    private State state = State.OFFLINE;

    public MessageEventHandler() {

    }

    private static boolean messageAuthorIsThisBot(User author) {
        return author.getIdLong() == DiscordBot.getJda().getSelfUser().getIdLong();
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (state != State.RUNNING) {
            return;
        }
        if (event.getAuthor().isBot()) {
            return;
        }
        handleCommand(event);
    }

    private void handleCommand(MessageReceivedEvent event) {
        String message = event.getMessage().getContentDisplay();
        if (message.toLowerCase(Locale.ROOT).startsWith(Command.IDENTIFIER)) {
            String[] split = message.split(" ");
            if (split.length > 1) {
                String command = split[1];
                String[] args = Arrays.copyOfRange(split, 2, split.length);
                commandHandler.handle(command, event, args);
            }
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        final JDA jda = DiscordBot.getJda();
        initialize();
        jda.getPresence().setActivity(Activity.of(Activity.ActivityType.LISTENING, Command.IDENTIFIER + " help"));
        setName(jda);
        setState(State.RUNNING);
    }

    private void setName(JDA jda) {
        String newName = Main.SETTINGS.getSetting(Setting.BOT_NAME);
        String currentName = jda.getSelfUser().getName();
        if (!newName.equals(currentName)) {
            logger.info("attempting to change bot name from \"{}\" to \"{}\" ", currentName, newName);
            jda.getSelfUser().getManager().setName(newName).queue();
        }
    }

    public void initialize() {
        setState(State.INITIALIZING);
        commandHandler.prepare();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            setState(State.SHUTTING_DOWN);
            DiscordBot.getJda().removeEventListener(this);
            logger.info("Shutting down bot");
            DiscordBot.getJda().shutdown();
            commandHandler.onShutDown();
            logger.info("Shutting down bot - DONE\n-------------------------------");
        }));
    }

    public State getState() {
        return state;
    }

    private void setState(State newState) {
        state = newState;
    }
}
