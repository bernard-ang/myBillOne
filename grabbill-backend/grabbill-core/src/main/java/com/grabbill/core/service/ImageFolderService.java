package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.ImageFolder;

import java.util.List;
import java.util.Optional;

/**
 * @author michael
 */
public interface ImageFolderService {

    boolean exist(Account account, String folderName);

    List<ImageFolder> getAllByAccount(Account account);

    Optional<ImageFolder> getByName(Account account, String folderName);

    ImageFolder save(ImageFolder imageFolder);

    void delete(ImageFolder imageFolder);

}
