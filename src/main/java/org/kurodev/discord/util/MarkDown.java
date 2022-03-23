package org.kurodev.discord.util;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author kuro
 **/
public enum MarkDown {
    ITALICS("*"),
    BOLD("**"),
    UNDERLINE("__"),
    STRIKETHROUGH("~~"),
    QUOTE("> ", " "),
    CODE("`"),
    CODE_BLOCK("```\n", "\n```"),
    ;

    private final String markdownStart;
    private final String markdownEnd;

    MarkDown(String start) {
        this(start, start);
    }


    MarkDown(String s, String end) {
        this.markdownStart = s;
        this.markdownEnd = end;
    }

    public String wrap(String content) {
        boolean isTextBlock = content.contains("\n") || content.contains("\r");
        if (isTextBlock && this == QUOTE) {
            return Arrays.stream(content.split("[\n\r]"))
                    .map(line -> markdownStart + line + markdownEnd + "\n")
                    .collect(Collectors.joining());
        } else {
            return markdownStart + content + (markdownEnd);
        }
    }

}
