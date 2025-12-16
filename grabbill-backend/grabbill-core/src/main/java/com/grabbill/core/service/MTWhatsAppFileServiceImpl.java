package com.grabbill.core.service;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppFile;
import com.grabbill.core.entity.MTWhatsAppType;
import com.grabbill.core.repository.MTWhatsAppFileRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class MTWhatsAppFileServiceImpl
        implements BaseFileService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppFile> {

    @Autowired
    private MTWhatsAppFileRepository repository;


    @Override
    public List<MTWhatsAppFile> getByActivity(
            final MTWhatsAppActivity mtWhatsAppActivity
    ) {
        return repository.findByMtWhatsAppActivity(mtWhatsAppActivity);
    }

    @Override
    public Optional<MTWhatsAppFile> getByNameAndActivity(
            final String filename,
            final MTWhatsAppActivity mtWhatsAppActivity
    ) {
        return repository.findByNameIsIgnoreCaseAndMtWhatsAppActivity(filename, mtWhatsAppActivity);
    }

    @Override
    public MTWhatsAppFile save(
            final MTWhatsAppFile mtWhatsAppFile
    ) {
        return repository.save(mtWhatsAppFile);
    }

    @Override
    public void delete(
            final MTWhatsAppFile mtWhatsAppFile
    ) {
        repository.delete(mtWhatsAppFile);
    }

}
