package com.grabbill.core.service;

import java.util.Properties;

/**
 * @author michaellow
 */
public class CustomImapService implements ImapService {

    @Override
    public Properties buildImapProperties(
            final String host,
            final int port
    ) {
        Properties properties = System.getProperties();

        properties.setProperty(PROTOCOL_KEY, PROTOCOL_VALUE);
        properties.put(HOST_KEY, host);
        properties.put(PORT_KEY, port);
        properties.setProperty(SOCKET_FACTORY_KEY, SOCKET_FACTORY_VALUE);
        properties.setProperty(SOCKET_FACTORY_FALLBACK_KEY, SOCKET_FACTORY_FALLBACK_VALUE);
        properties.setProperty(SSL_TRUST_KEY, SSL_TRUST_VALUE);
        properties.setProperty(SSL_ENABLE_KEY,  SSL_ENABLE_VALUE);
        properties.setProperty(SOCKET_FACTORY_PORT_KEY, String.valueOf(port));

        return properties;
    }

}
