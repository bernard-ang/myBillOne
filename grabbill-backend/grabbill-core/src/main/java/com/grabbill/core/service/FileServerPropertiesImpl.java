package com.grabbill.core.service;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author michaellow
 */
public class FileServerPropertiesImpl implements FileServerProperties {

    @Value("${storage.url}")
    private String url;

    @Value("${storage.access-key}")
    private String accessKey;

    @Value("${storage.secret-key}")
    private String secretKey;

    @Value("${storage.bucket}")
    private String bucket;


    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public String getAccessKey() {
        return this.accessKey;
    }

    @Override
    public String getSecretKey() {
        return this.secretKey;
    }

    @Override
    public String getBucket() {
        return this.bucket;
    }

}
