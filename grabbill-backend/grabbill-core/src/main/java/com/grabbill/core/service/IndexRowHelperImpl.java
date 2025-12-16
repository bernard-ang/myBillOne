package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DataType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author michaellow
 */
public class IndexRowHelperImpl implements IndexRowHelper {

    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String getText(
            final int index,
            final BaseIndexRow indexRow
    ) {
        verifyIndex(index);

        String text = null;
        if (index == 1) {
            text = indexRow.getText1();

        } else if (index == 2) {
            text = indexRow.getText2();

        } else if (index == 3) {
            text = indexRow.getText3();

        } else if (index == 4) {
            text = indexRow.getText4();

        } else if (index == 5) {
            text = indexRow.getText5();

        } else if (index == 6) {
            text = indexRow.getText6();

        } else if (index == 7) {
            text = indexRow.getText7();

        } else if (index == 8) {
            text = indexRow.getText8();

        } else if (index == 9) {
            text = indexRow.getText9();

        } else if (index == 10) {
            text = indexRow.getText10();
        }

        return text;
    }

    @Override
    public Integer getNumber(final int index, final BaseIndexRow indexRow) {
        verifyIndex(index);

        Integer number = null;
        if (index == 1) {
            number = indexRow.getNumber1();

        } else if (index == 2) {
            number = indexRow.getNumber2();

        } else if (index == 3) {
            number = indexRow.getNumber3();

        } else if (index == 4) {
            number = indexRow.getNumber4();

        } else if (index == 5) {
            number = indexRow.getNumber5();

        } else if (index == 6) {
            number = indexRow.getNumber6();

        } else if (index == 7) {
            number = indexRow.getNumber7();

        } else if (index == 8) {
            number = indexRow.getNumber8();

        } else if (index == 9) {
            number = indexRow.getNumber9();

        } else if (index == 10) {
            number = indexRow.getNumber10();
        }

        return number;
    }

    @Override
    public LocalDate getDate(final int index, final BaseIndexRow indexRow) {
        verifyIndex(index);

        LocalDate date = null;
        if (index == 1) {
            date = indexRow.getDate1();

        } else if (index == 2) {
            date = indexRow.getDate2();

        } else if (index == 3) {
            date = indexRow.getDate3();

        } else if (index == 4) {
            date = indexRow.getDate4();

        } else if (index == 5) {
            date = indexRow.getDate5();

        } else if (index == 6) {
            date = indexRow.getDate6();

        } else if (index == 7) {
            date = indexRow.getDate7();

        } else if (index == 8) {
            date = indexRow.getDate8();

        } else if (index == 9) {
            date = indexRow.getDate9();

        } else if (index == 10) {
            date = indexRow.getDate10();
        }

        return date;
    }

    private void verifyIndex(final int index) {
        if (index > 10) {
            throw new IllegalArgumentException("Max index is 10");
        }
    }

    @Override
    public Map<String, String> buildTransactionalEmailParameterMap(
            final TransactionalEmailIndexRow indexRow,
            final List<TransactionalEmailIndexField> indexFields
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        for (int i = 0; i < indexFields.size(); i++) {
            TransactionalEmailIndexField indexField = indexFields.get(i);
            if (DataType.TEXT.equals(indexField.getDataType()) || DataType.EMAIL.equals(indexField.getDataType())) {
                String text = this.getText(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), text != null ? text : "");

            } else if (DataType.NUMBER.equals(indexField.getDataType())) {
                Integer number = this.getNumber(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), number != null ? String.valueOf(number) : "");

            } else if (DataType.DATE.equals(indexField.getDataType())) {
                LocalDate date = this.getDate(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), date != null ? DATE_TIME_FORMATTER.format(date) : "");
            }
        }

        return parameterMap;
    }

    @Override
    public Map<String, String> buildMtTransactionalEmailParameterMap(
            final MTTransactionalEmailIndexRow indexRow,
            final List<MTTransactionalEmailIndexField> indexFields
    ) {
        Map<String, String> parameterMap = new HashMap<>();
        for (int i = 0; i < indexFields.size(); i++) {
            MTTransactionalEmailIndexField indexField = indexFields.get(i);
            if (DataType.TEXT.equals(indexField.getDataType()) || DataType.EMAIL.equals(indexField.getDataType())) {
                String text = this.getText(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), text != null ? text : "");

            } else if (DataType.NUMBER.equals(indexField.getDataType())) {
                Integer number = this.getNumber(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), number != null ? String.valueOf(number) : "");

            } else if (DataType.DATE.equals(indexField.getDataType())) {
                LocalDate date = this.getDate(i + 1, indexRow);
                parameterMap.put(indexField.getHeader(), date != null ? DATE_TIME_FORMATTER.format(date) : "");
            }
        }

        return parameterMap;
    }
}
