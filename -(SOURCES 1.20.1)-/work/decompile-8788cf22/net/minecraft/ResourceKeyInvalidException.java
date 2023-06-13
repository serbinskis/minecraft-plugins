package net.minecraft;

import org.apache.commons.lang3.StringEscapeUtils;

public class ResourceKeyInvalidException extends RuntimeException {

    public ResourceKeyInvalidException(String s) {
        super(StringEscapeUtils.escapeJava(s));
    }

    public ResourceKeyInvalidException(String s, Throwable throwable) {
        super(StringEscapeUtils.escapeJava(s), throwable);
    }
}
