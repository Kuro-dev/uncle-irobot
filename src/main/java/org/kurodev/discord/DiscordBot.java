package org.kurodev.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.kurodev.Main;
import org.kurodev.config.Setting;
import org.kurodev.discord.message.MessageEventHandler;
import org.kurodev.discord.util.MarkDown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;


public class DiscordBot implements Runnable {
    private static JDA jda;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageEventHandler messageEventHandler= new MessageEventHandler();;

    public static JDA getJda() {
        return jda;
    }

    @Override
    public void run() {
        try {
            jda = JDABuilder.createDefault(Main.SETTINGS.getSetting(Setting.TOKEN)).build();
            jda.addEventListener(messageEventHandler);
            MarkDown.CODE_BLOCK.wrap("hallo");
        } catch (LoginException e) {
            logger.error("Failed to start discord bot", e);
        }
    }
}
