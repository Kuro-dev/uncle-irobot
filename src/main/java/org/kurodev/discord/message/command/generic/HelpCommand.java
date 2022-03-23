package org.kurodev.discord.message.command.generic;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;
import org.kurodev.discord.message.command.Command;
import org.kurodev.discord.util.MarkDown;

import java.util.List;

/**
 * @author kuro
 **/
public class HelpCommand extends GenericCommand {
    private final List<Command> commands;

    public HelpCommand(List<Command> commands) {
        super("Help");
        this.commands = commands;
    }

    @Override
    protected void prepare(Options args) throws Exception {
    }

    @Override
    public String getDescription() {
        return "Lists all available commands. use " + Command.IDENTIFIER + " help *command* for additional info";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void execute(MessageChannel channel, CommandLine args, @NotNull MessageReceivedEvent event) {
        String[] unrecognized = args.getArgs();
        if (unrecognized.length > 0) {
            for (String str : unrecognized) {
                Command com = find(str);
                if (com != null) {
                    MessageAction action = channel.sendMessage(com.getCommand())
                            .append(" - `").append(com.getDescription()).append("`\n");
                    String possibleArgs = com.getArgumentsAsString();
                    if (!possibleArgs.isBlank())
                        action.append("\nArguments:\n```\n").append(possibleArgs).append("\n```");
                    action.queue();
                } else {
                    channel.sendMessage("Command unknown").queue();
                }
                return;
            }
        }
        //now list all commands
        StringBuilder out = new StringBuilder();
        for (Command command : commands) {
            out.append(command.getCommand()).append(", ");
        }
        channel.sendMessage(MarkDown.CODE_BLOCK.wrap(out.toString())).queue();
    }

    private Command find(String name) {
        return commands.stream().filter(command -> command.getCommand().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
