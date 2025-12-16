package com.grabbill.core.service;

/**
 * @author michaellow
 */
public interface MessageIdGenerator {

    String PREFIX = "<";
    String SUFFIX = "@grabbill.com>";

    String generate(String rawMessageId);

    String parse(String fullMessageId);

    boolean isGrabbillMessageId(String fullMessageId);

}
