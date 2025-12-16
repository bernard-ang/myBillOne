package com.grabbill.core.service;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingFile;
import com.grabbill.core.entity.DigitalFilingType;
import com.grabbill.core.repository.DigitalFilingFileRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class DigitalFilingFileServiceImpl
        implements BaseFileService<DigitalFilingType, DigitalFilingActivity, DigitalFilingFile> {

    @Autowired
    private DigitalFilingFileRepository repository;


    @Override
    public List<DigitalFilingFile> getByActivity(
            final DigitalFilingActivity digitalFilingActivity
    ) {
        return repository.findByDigitalFilingActivity(digitalFilingActivity);
    }

    @Override
    public Optional<DigitalFilingFile> getByNameAndActivity(
            final String filename,
            final DigitalFilingActivity digitalFilingActivity
    ) {
        return repository.findByNameIsIgnoreCaseAndDigitalFilingActivity(filename, digitalFilingActivity);
    }

    @Override
    public DigitalFilingFile save(final DigitalFilingFile digitalFilingFile) {
        return repository.save(digitalFilingFile);
    }

    @Override
    public void delete(
            final DigitalFilingFile digitalFilingFile
    ) {
        repository.delete(digitalFilingFile);
    }

}
