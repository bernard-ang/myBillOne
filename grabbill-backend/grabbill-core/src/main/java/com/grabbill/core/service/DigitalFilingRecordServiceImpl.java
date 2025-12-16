package com.grabbill.core.service;

import com.grabbill.core.entity.DigitalFilingRecord;
import com.grabbill.core.repository.DigitalFilingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author michaellow
 */
public class DigitalFilingRecordServiceImpl implements BaseRecordService<DigitalFilingRecord> {

    @Autowired
    private DigitalFilingRecordRepository repository;


    @Override
    public DigitalFilingRecord save(final DigitalFilingRecord digitalFilingRecord) {
        return repository.save(digitalFilingRecord);
    }

}
