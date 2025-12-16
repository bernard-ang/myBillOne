package com.grabbill.core.service;

/**
 * @author michaellow
 */
public class MessageIdGeneratorImpl implements MessageIdGenerator {

    @Override
    public String generate(final String rawMessageId) {
        return PREFIX + rawMessageId + SUFFIX;
    }

    @Override
    public String parse(final String fullMessageId) {
        if (fullMessageId.startsWith(PREFIX) && fullMessageId.endsWith(SUFFIX)) {
            return fullMessageId.substring(1, fullMessageId.length() - SUFFIX.length());
        }

        return fullMessageId;
    }

    @Override
    public boolean isGrabbillMessageId(final String fullMessageId) {
        if (fullMessageId.startsWith(PREFIX) && fullMessageId.endsWith(SUFFIX)) {
            return true;
        }

        return false;
    }
}
