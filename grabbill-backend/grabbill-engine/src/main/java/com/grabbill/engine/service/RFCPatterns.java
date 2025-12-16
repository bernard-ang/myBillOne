package com.grabbill.engine.service;

import java.util.regex.Pattern;

/**
 * @author michaellow
 */
public interface RFCPatterns {

    Pattern RFC1893_PATTERN = Pattern.compile("\\s([245]\\.\\d{1,3}\\.\\d{1,3})\\s", Pattern.DOTALL);

    Pattern RFC2821_PATTERN = Pattern.compile("\\s([245]\\d\\d)\\s", Pattern.DOTALL);

}
