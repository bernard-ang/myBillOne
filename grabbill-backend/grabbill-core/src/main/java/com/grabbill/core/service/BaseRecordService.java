package com.grabbill.core.service;

import com.grabbill.core.entity.BaseRecord;

/**
 * @author michaellow
 * @param <R> BaseRecord
 */
public interface BaseRecordService<R extends BaseRecord> {

    R save(R record);

}
