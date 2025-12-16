package com.grabbill.core.service;

import com.grabbill.core.entity.SmsRecord;
import com.grabbill.core.repository.SmsRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author michaellow
 */
public class SmsRecordServiceImpl implements BaseRecordService<SmsRecord> {

    @Autowired
    private SmsRecordRepository repository;


    @Override
    public SmsRecord save(final SmsRecord smsRecord) {
        return repository.save(smsRecord);
    }

}
