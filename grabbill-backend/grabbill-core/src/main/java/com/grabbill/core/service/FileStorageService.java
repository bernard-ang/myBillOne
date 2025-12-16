package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.storage.FileObjectType;
import io.minio.messages.Item;

/**
 * @author michaellow
 */
public interface FileStorageService {

    boolean upload(
            Account account,
            FileObjectType type,
            String directory,
            String filename,
            byte[] bytes
    ) throws GrabbillException;

    byte[] download(
            Account account,
            FileObjectType type,
            String directory,
            String filename
    ) throws GrabbillException;

    void delete(
            Account account,
            FileObjectType type,
            String directory,
            String filename
    ) throws GrabbillException;

    Item find(
            Account account,
            FileObjectType type,
            String directory,
            String filename
    ) throws GrabbillException;

}
