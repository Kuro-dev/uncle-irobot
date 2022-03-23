package org.kurodev.discord.message.command;

public interface Preparable {
    /**
     * Prepare everything that is necessary for this command to work here. Especially things, that can fail, like file
     * operations etc. Will be invoked once during the start of the bot. May be invoked again at a later time.
     *
     * @throws Exception if the preparation fails meaning the command will not at all be usable. Thus will be removed
     *                   from the available commands list to avoid exceptions
     */
    default void prepare() throws Exception {

    }
}
