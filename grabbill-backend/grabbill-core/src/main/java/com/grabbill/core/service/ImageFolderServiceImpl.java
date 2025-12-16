package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.ImageFolder;
import com.grabbill.core.repository.ImageFolderRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class ImageFolderServiceImpl implements ImageFolderService {

    @Autowired
    private ImageFolderRepository repository;

    @Override
    public boolean exist(
            final Account account,
            final String folderName
    ) {
        return repository.findByAccountIsAndNameIs(account, folderName).isPresent();
    }

    @Override
    public List<ImageFolder> getAllByAccount(final Account account) {
        return repository.findAllByAccountIs(account);
    }

    @Override
    public Optional<ImageFolder> getByName(
            final Account account,
            final String folderName
    ) {
        return repository.findByAccountIsAndNameIs(account, folderName);
    }

    @Override
    public ImageFolder save(ImageFolder imageFolder) {
        return repository.save(imageFolder);
    }

    @Override
    public void delete(ImageFolder imageFolder) {
        repository.delete(imageFolder);
    }

}
