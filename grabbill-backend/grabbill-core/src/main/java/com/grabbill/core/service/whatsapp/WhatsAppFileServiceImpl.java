package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppFile;
import com.grabbill.core.entity.WhatsAppType;
import com.grabbill.core.repository.WhatsAppFileRepository;
import com.grabbill.core.service.BaseFileService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public class WhatsAppFileServiceImpl
        implements BaseFileService<WhatsAppType, WhatsAppActivity, WhatsAppFile> {

    @Autowired
    private WhatsAppFileRepository repository;


    @Override
    public List<WhatsAppFile> getByActivity(
            final WhatsAppActivity whatsAppActivity
    ) {
        return repository.findByWhatsAppActivity(whatsAppActivity);
    }

    @Override
    public Optional<WhatsAppFile> getByNameAndActivity(
            final String filename,
            final WhatsAppActivity whatsAppActivity
    ) {
        return repository.findByNameIsIgnoreCaseAndWhatsAppActivity(filename, whatsAppActivity);
    }

    @Override
    public WhatsAppFile save(
            final WhatsAppFile whatsAppFile
    ) {
        return repository.save(whatsAppFile);
    }

    @Override
    public void delete(
            final WhatsAppFile whatsAppFile
    ) {
        repository.delete(whatsAppFile);
    }

}
