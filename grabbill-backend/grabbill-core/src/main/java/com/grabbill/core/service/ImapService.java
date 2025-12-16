package com.grabbill.core.service;

import java.util.Properties;

/**
 * @author michaellow
 */
public interface ImapService {

    String INBOX = "inbox";
    String PROTOCOL_KEY = "mail.store.protocol";
    String PROTOCOL_VALUE = "imap";
    String HOST_KEY = "mail.imap.host";
    String PORT_KEY = "mail.imap.port";

    String SOCKET_FACTORY_KEY = "mail.imap.socketFactory.class";
    String SOCKET_FACTORY_VALUE = "javax.net.ssl.SSLSocketFactory";
    String SOCKET_FACTORY_FALLBACK_KEY = "mail.imap.socketFactory.fallback";
    String SOCKET_FACTORY_FALLBACK_VALUE = "false";
    String SOCKET_FACTORY_PORT_KEY = "mail.imap.socketFactory.port";
    String SSL_TRUST_KEY = "mail.imap.ssl.trust";
    String SSL_TRUST_VALUE = "*";
    String SSL_ENABLE_KEY = "mail.imap.ssl.enable";
    String SSL_ENABLE_VALUE = "true";


    Properties buildImapProperties(String host, int port);

}
