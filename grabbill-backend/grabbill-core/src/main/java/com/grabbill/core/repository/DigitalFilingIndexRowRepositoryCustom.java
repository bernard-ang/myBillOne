package com.grabbill.core.repository;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingIndexRow;
import com.grabbill.core.entity.DigitalFilingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface DigitalFilingIndexRowRepositoryCustom {

    Page<DigitalFilingIndexRow> searchByFilters(
            DigitalFilingType digitalFilingType,
            Optional<DigitalFilingActivity> digitalFilingActivityOptional,
            Map<String, String> filters,
            boolean hasFileOnly,
            Pageable pageable
    );

}
