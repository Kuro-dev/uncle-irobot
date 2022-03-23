package org.kurodev.discord.message.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker Annotation to denote any Command that is to be loaded by the bot at startup.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoRegister {
    /**
     * @return true if the command should be loaded, false otherwise.
     */
    boolean load() default true;
}
