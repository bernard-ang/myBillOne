package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BaseFile;
import com.grabbill.core.entity.BaseIndexRow;
import com.grabbill.core.entity.BaseRecord;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author michaellow
 */
@Data
public class BaseIndexRowFullPayload implements ApiPayload {

    Long indexRowId;
    int seqOrder;
    String text1;
    Integer number1;
    LocalDate date1;
    String text2;
    Integer number2;
    LocalDate date2;
    String text3;
    Integer number3;
    LocalDate date3;
    String text4;
    Integer number4;
    LocalDate date4;
    String text5;
    Integer number5;
    LocalDate date5;
    String text6;
    Integer number6;
    LocalDate date6;
    String text7;
    Integer number7;
    LocalDate date7;
    String text8;
    Integer number8;
    LocalDate date8;
    String text9;
    Integer number9;
    LocalDate date9;
    String text10;
    Integer number10;
    LocalDate date10;

    BaseFilePayload file;

    Long recordId;
    String recordName;
    Integer priority;
    Integer retryCount;
    String message;
    ProcessStatus status;

    Long activityId;
    String activityName;

    protected void copyFrom(final BaseIndexRow indexRow) {
        this.setSeqOrder(indexRow.getSeqOrder());
        this.setText1(indexRow.getText1());
        this.setNumber1(indexRow.getNumber1());
        this.setDate1(indexRow.getDate1());

        this.setText2(indexRow.getText2());
        this.setNumber2(indexRow.getNumber2());
        this.setDate2(indexRow.getDate2());

        this.setText3(indexRow.getText3());
        this.setNumber3(indexRow.getNumber3());
        this.setDate3(indexRow.getDate3());

        this.setText4(indexRow.getText4());
        this.setNumber4(indexRow.getNumber4());
        this.setDate4(indexRow.getDate4());

        this.setText5(indexRow.getText5());
        this.setNumber5(indexRow.getNumber5());
        this.setDate5(indexRow.getDate5());

        this.setText6(indexRow.getText6());
        this.setNumber6(indexRow.getNumber6());
        this.setDate6(indexRow.getDate6());

        this.setText7(indexRow.getText7());
        this.setNumber7(indexRow.getNumber7());
        this.setDate7(indexRow.getDate7());

        this.setText8(indexRow.getText8());
        this.setNumber8(indexRow.getNumber8());
        this.setDate8(indexRow.getDate8());

        this.setText9(indexRow.getText9());
        this.setNumber9(indexRow.getNumber9());
        this.setDate9(indexRow.getDate9());

        this.setText10(indexRow.getText10());
        this.setNumber10(indexRow.getNumber10());
        this.setDate10(indexRow.getDate10());
    }

    protected void copyFrom(final Long id, final BaseFile file) {
        BaseFilePayload filePayload = new BaseFilePayload();
        filePayload.setId(id);
        filePayload.copyFrom(file);
        this.setFile(filePayload);
    }

    protected void copyFrom(final BaseRecord record) {
        this.setRecordName(record.getName());
        this.setPriority(record.getPriority());
        this.setRetryCount(record.getRetryCount());
        this.setMessage(record.getMessage());
        this.setStatus(record.getStatus());
    }

}
