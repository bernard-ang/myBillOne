package com.grabbill.core.service;

import com.grabbill.core.entity.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author michaellow
 */
public interface IndexRowHelper {

    String getText(int index, BaseIndexRow indexRow);

    Integer getNumber(int index, BaseIndexRow indexRow);

    LocalDate getDate(int index, BaseIndexRow indexRow);

    Map<String, String> buildTransactionalEmailParameterMap(
            TransactionalEmailIndexRow indexRow,
            List<TransactionalEmailIndexField> indexFields
    );

    Map<String, String> buildMtTransactionalEmailParameterMap(
            MTTransactionalEmailIndexRow indexRow,
            List<MTTransactionalEmailIndexField> indexFields
    );

}
