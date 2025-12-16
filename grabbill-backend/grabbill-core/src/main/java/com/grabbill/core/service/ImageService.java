package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Image;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface ImageService {

    int UUID_LENGTH = 20;


    String generateLinkId();

    List<Image> getByAccount(Account account);

    Optional<Image> getByAccountAndId(Account account, Long id);

    List<Image> getByAccountAndImageFolderName(Account account, String imageFolderName);

    List<Image> getByAccountAndImageFolderIsNull(Account account);

    List<Image> getByAccountAndFileNameAndImageFolderName(Account account, String fileName, String imageFolderName);

    List<Image> getByAccountAndFileNameAndImageFolderIsNull(Account account, String fileName);

    Optional<Image> getByLinkIdAndFileName(String linkId, String fileName);

    void delete(Image image);

    Image save(Image image);

}
