package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Image;
import com.grabbill.core.repository.ImageRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository repository;


    @Override
    public String generateLinkId() {
        return RandomStringUtils.randomAlphanumeric(UUID_LENGTH);
    }

    @Override
    public List<Image> getByAccount(final Account account) {
        return repository.findAllByAccountIs(account);
    }

    @Override
    public Optional<Image> getByAccountAndId(final Account account, final Long id) {
        return repository.findByAccountIsAndIdIs(account, id);
    }

    @Override
    public List<Image> getByAccountAndImageFolderName(
            final Account account,
            final String imageFolderName
    ) {
        return repository.findByAccountIsAndImageFolder_Name(account, imageFolderName);
    }

    @Override
    public List<Image> getByAccountAndImageFolderIsNull(
            final Account account
    ) {
        return repository.findByAccountIsAndImageFolderIsNull(account);
    }

    @Override
    public List<Image> getByAccountAndFileNameAndImageFolderName(
            final Account account,
            final String fileName,
            final String imageFolderName
    ) {
        return repository.findByAccountIsAndFilenameIsAndImageFolder_Name(
                account,
                fileName,
                imageFolderName
        );
    }

    @Override
    public List<Image> getByAccountAndFileNameAndImageFolderIsNull(
            final Account account,
            final String fileName
    ) {
        return repository.findByAccountIsAndFilenameIsAndImageFolderIsNull(
                account,
                fileName
        );
    }

    @Override
    public Optional<Image> getByLinkIdAndFileName(
            final String linkId,
            final String fileName
    ) {
        return repository.findByLinkIdIsAndFilenameIs(linkId, fileName);
    }

    @Override
    public void delete(final Image image) {
        repository.delete(image);
    }

    @Override
    public Image save(final Image image) {
        return repository.save(image);
    }

}
