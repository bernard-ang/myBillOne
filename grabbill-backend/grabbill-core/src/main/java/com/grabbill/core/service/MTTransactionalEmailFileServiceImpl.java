package com.grabbill.core.service;

import com.grabbill.core.entity.MTTransactionalEmailActivity;
import com.grabbill.core.entity.MTTransactionalEmailFile;
import com.grabbill.core.entity.MTTransactionalEmailType;
import com.grabbill.core.repository.MTTransactionalEmailFileRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class MTTransactionalEmailFileServiceImpl
        implements BaseFileService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailFile> {

    @Autowired
    private MTTransactionalEmailFileRepository repository;


    @Override
    public List<MTTransactionalEmailFile> getByActivity(
            final MTTransactionalEmailActivity mtTransactionalEmailActivity
    ) {
        return repository.findByMtTransactionalEmailActivity(mtTransactionalEmailActivity);
    }

    @Override
    public Optional<MTTransactionalEmailFile> getByNameAndActivity(
            final String filename,
            final MTTransactionalEmailActivity mtTransactionalEmailActivity
    ) {
        return repository.findByNameIsIgnoreCaseAndMtTransactionalEmailActivity(filename, mtTransactionalEmailActivity);
    }

    @Override
    public MTTransactionalEmailFile save(
            final MTTransactionalEmailFile mtTransactionalEmailFile
    ) {
        return repository.save(mtTransactionalEmailFile);
    }

    @Override
    public void delete(
            final MTTransactionalEmailFile mtTransactionalEmailFile
    ) {
        repository.delete(mtTransactionalEmailFile);
    }

}
