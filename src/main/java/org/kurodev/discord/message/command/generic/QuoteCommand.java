package org.kurodev.discord.message.command.generic;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NotNull;
import org.kurodev.Main;
import org.kurodev.config.Setting;
import org.kurodev.discord.message.command.AutoRegister;
import org.kurodev.discord.util.Cache;
import org.kurodev.discord.util.MarkDown;
import org.kurodev.discord.util.handlers.TextSampleHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author kuro
 **/
@AutoRegister
public class QuoteCommand extends GenericCommand {
    private static final Pattern IS_NUMERIC_REG = Pattern.compile("\\d+");
    private final Path quotesFolder = Paths.get(Main.SETTINGS.getSetting(Setting.QUOTES_FOLDER));
    private final Map<String, Cache<TextSampleHandler>> samples = new HashMap<>();
    private final Random random = new Random();
    private String languageList = "";

    public QuoteCommand() {
        super("quote");
    }

    @Override
    public void prepare(Options args) throws Exception {
        args.addOption("l", "lang", true, "language of the quote");
        args.addOption("languages", "lists all available languages");
        Files.walk(quotesFolder).filter(Files::isRegularFile).forEach(path -> {
            samples.put(path.getFileName().toString().replace(".txt", ""),
                    new Cache<>(6, TimeUnit.HOURS));
        });
        StringBuilder newList = new StringBuilder();
        samples.forEach((key, textSampleHandlerCache) -> {
            newList.append(key)
                    .append(", ");
        });
        languageList = newList.toString();
    }

    @Override
    public void execute(MessageChannel channel, CommandLine args, @NotNull MessageReceivedEvent event) {
        if (args.hasOption("languages")) {
            channel.sendMessage(MarkDown.CODE_BLOCK.wrap(languageList)).queue();
            return;
        }
        final String lang = args.hasOption("l") ? args.getOptionValue("l") : "en";
        var sample = samples.get(lang);
        if (sample.isDirty()) {
            if (!refreshSample(lang, sample)) {
                channel.sendMessage("Something went wrong when trying to fetch the quotes :(").queue();
            }
        }
        channel.sendMessage(MarkDown.QUOTE.wrap(sample.getCachedItem().getRandomLine()))
                .append(System.lineSeparator()).append("- Uncle Iroh")
                .queue();
    }

    private boolean refreshSample(String lang, Cache<TextSampleHandler> cache) {
        var handler = new TextSampleHandler(quotesFolder.resolve(lang + ".txt"));
        try {
            handler.prepare();
            cache.update(handler);
            return true;
        } catch (IOException e) {
            logger.error("failed loading samples from text file", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "sends a random quote from out beloved uncle";
    }
}
