package org.kurodev.discord.message.command;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;
import org.kurodev.discord.message.State;

import java.io.IOException;

/**
 * @author kuro
 **/
public interface Command extends Preparable {
    String IDENTIFIER = "!iroh";


    default void prepare() {

    }

    default void onShutdown() throws Exception {

    }

    default boolean check(String command, MessageReceivedEvent event) {
        return isOnline() && getCommand().equalsIgnoreCase(command);
    }

    void execute(MessageChannel channel, CommandLine args, @NotNull MessageReceivedEvent event) throws IOException;

    default boolean needsAdmin() {
        return false;
    }

    String getCommand();

    default boolean isListed() {
        return true;
    }

    String getDescription();

    default boolean supportsMention() {
        return false;
    }

    Options getOptions();

    State getState();

    default boolean isOnline() {
        return getState() == State.RUNNING;
    }

    String getArgumentsAsString();

}
