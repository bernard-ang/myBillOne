package com.grabbill.core.service;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailFile;
import com.grabbill.core.entity.TransactionalEmailType;
import com.grabbill.core.repository.TransactionalEmailFileRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class TransactionalEmailFileServiceImpl
        implements BaseFileService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailFile> {

    @Autowired
    private TransactionalEmailFileRepository repository;


    @Override
    public List<TransactionalEmailFile> getByActivity(
            final TransactionalEmailActivity transactionalEmailActivity
    ) {
        return repository.findByTransactionalEmailActivity(transactionalEmailActivity);
    }

    @Override
    public Optional<TransactionalEmailFile> getByNameAndActivity(
            final String filename,
            final TransactionalEmailActivity transactionalEmailActivity
    ) {
        return repository.findByNameIsIgnoreCaseAndTransactionalEmailActivity(filename, transactionalEmailActivity);
    }

    @Override
    public TransactionalEmailFile save(
            final TransactionalEmailFile transactionalEmailFile
    ) {
        return repository.save(transactionalEmailFile);
    }

    @Override
    public void delete(
            final TransactionalEmailFile transactionalEmailFile
    ) {
        repository.delete(transactionalEmailFile);
    }

}
