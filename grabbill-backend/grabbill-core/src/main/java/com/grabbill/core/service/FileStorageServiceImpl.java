package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.storage.FileObjectType;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author michaellow
 */
public class FileStorageServiceImpl implements FileStorageService {

    private static final String DASH = "-";
    private static final String SLASH = "/";
    private final FileServerProperties fileServerProperties;
    private final MinioClient minioClient;


    public FileStorageServiceImpl(final FileServerProperties fileServerProperties) {
        this.fileServerProperties = fileServerProperties;
        this.minioClient = MinioClient.builder()
                .endpoint(fileServerProperties.getUrl())
                .credentials(
                        fileServerProperties.getAccessKey(),
                        fileServerProperties.getSecretKey()
                )
                .build();

        this.createBucketIfNotExists(fileServerProperties.getBucket());
    }

    void createBucketIfNotExists(final String bucketName) throws GrabbillException {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new GrabbillException(
                    "Failed to create master storage bucket - [" + bucketName + "].", e);
        }
    }

    @Override
    public boolean upload(
            final Account account,
            final FileObjectType type,
            final String directory,
            final String filename,
            final byte[] bytes
    ) throws GrabbillException {

        String bucketName = this.fileServerProperties.getBucket();
        String objectName = getObjectName(account, type, directory, filename);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(bais, bais.available(), -1)
                            .build()
            );

            return true;

        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new GrabbillException(
                    "Failed to upload file [" + objectName + "] to bucket [" + bucketName + "].", e);
        }

    }

    @Override
    public Item find(
            final Account account,
            final FileObjectType type,
            final String directory,
            final String filename
    ) throws GrabbillException {

        String bucketName = this.fileServerProperties.getBucket();
        String prefix = getPrefix(account, type, directory);
        String objectName = getObjectName(account, type, directory, filename);

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .prefix(prefix)
                        .build()
        );

        Item target = null;
        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.objectName().equals(objectName)) {
                    target = item;
                    break;
                }
            }

        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new GrabbillException(
                    "Failed to locate file [" + objectName + "] in bucket [" + bucketName + "].", e);
        }

        return target;
    }

    @Override
    public byte[] download(
            final Account account,
            final FileObjectType type,
            final String directory,
            final String filename
    ) throws GrabbillException {

        String bucketName = this.fileServerProperties.getBucket();
        String objectName = getObjectName(account, type, directory, filename);

        byte[] bytes = null;
        Item item = find(account, type, directory, filename);
        if (item != null) {
            try {
                bytes = IOUtils.toByteArray(
                        minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(objectName)
                                        .build()
                        )
                );

            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new GrabbillException(
                        "Failed to download file [" + objectName + "] from bucket [" + bucketName + "].", e);
            }
        }

        return bytes;
    }

    @Override
    public void delete(
            final Account account,
            final FileObjectType type,
            final String directory,
            final String filename
    ) throws GrabbillException {

        String bucketName = this.fileServerProperties.getBucket();
        String objectName = getObjectName(account, type, directory, filename);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new GrabbillException(
                    "Failed to delete file [" + objectName + "] from bucket [" + bucketName + "].", e);
        }
    }

    private String getObjectName(
            final Account account,
            final FileObjectType type,
            final String directory,
            final String filename
    ) {
        StringBuilder stringBuilder = new StringBuilder();
        buildPrefix(stringBuilder, account, type, directory);
        stringBuilder.append(filename);

        return stringBuilder.toString();
    }

    private String getPrefix(
            final Account account,
            final FileObjectType type,
            final String directory
    ) {
        StringBuilder stringBuilder = new StringBuilder();
        buildPrefix(stringBuilder, account, type, directory);

        return stringBuilder.toString();
    }

    private void buildPrefix(
            final StringBuilder stringBuilder,
            final Account account,
            final FileObjectType type,
            final String directory
    ) {
        stringBuilder.append(account.getClass().getSimpleName().toLowerCase())
                .append(DASH)
                .append(account.getId())
                .append(SLASH)
                .append(type.getShortname())
                .append(SLASH);
        if (StringUtils.hasLength(directory)) {
            stringBuilder.append(directory)
                    .append(SLASH);
        }
    }

}
