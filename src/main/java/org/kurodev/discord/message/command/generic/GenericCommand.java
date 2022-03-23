package org.kurodev.discord.message.command.generic;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;
import org.kurodev.Main;
import org.kurodev.config.Setting;
import org.kurodev.discord.message.State;
import org.kurodev.discord.message.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author kuro
 **/
public abstract class GenericCommand implements Command {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final EnumSet<Permission> neededPermissions = EnumSet.noneOf(Permission.class);
    private final Options args = new Options();
    private final String command;
    private State state = State.OFFLINE;

    public GenericCommand(String command, Permission... neededPermissions) {
        this.command = command;
        this.neededPermissions.add(Permission.MESSAGE_WRITE);
        this.neededPermissions.add(Permission.MESSAGE_READ);
        if (neededPermissions.length > 0)
            this.neededPermissions.addAll(Arrays.asList(neededPermissions));
    }

    @Override
    public String toString() {
        return "GenericCommand{" +
                "command='" + command + '\'' +
                ", state=" + state +
                '}';
    }

    @Override
    public final void prepare() {
        setState(State.INITIALIZING);
        try {
            prepare(args);
            setState(State.RUNNING);
        } catch (Exception e) {
            setState(State.OFFLINE);
            logger.error("Failed to initialize " + this, e);
        }
    }

    protected void prepare(Options args) throws Exception {
    }

    protected String getSetting(Setting setting) {
        return Main.SETTINGS.getSetting(setting);
    }

    protected boolean getSettingBool(Setting setting) {
        return Main.SETTINGS.getSettingBool(setting);
    }

    @Override
    public final Options getOptions() {
        return args;
    }

    public final String getCommand() {
        return command;
    }

    /**
     * @return an array of permissions that are missing for this command.
     */
    public final Permission[] checkPermissions(GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        TextChannel channel = event.getChannel();
        return neededPermissions.stream()
                .filter(neededPermission ->
                        guild.getSelfMember().getPermissions(channel).contains(neededPermission))
                .toArray(Permission[]::new);
    }

    @Override
    public State getState() {
        return state;
    }

    private void setState(State newState) {
        state = newState;
    }

    @Override
    public final String getArgumentsAsString() {
        if (args.getOptions().isEmpty()) {
            return "There are no arguments for " + command;
        }
        final HelpFormatter formatter = new HelpFormatter();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String syntax = Command.IDENTIFIER + " " + command + " <arguments>";
        try (PrintWriter pw = new PrintWriter(baos, false, StandardCharsets.UTF_8)) {
            formatter.printHelp(pw, 200, syntax,
                    "All arguments:", args, 2, 5, "");
            pw.flush();
        }
        return baos.toString(StandardCharsets.UTF_8);
    }


    protected boolean botHasPermission(@NotNull MessageReceivedEvent event) {
        return event.getGuild().getSelfMember().hasPermission(neededPermissions);
    }
}
