package com.grabbill.core.model.sftp;

import com.grabbill.core.entity.BaseIndexRow;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author michaellow
 */
@Data
public class ExcelIndexRow {

    private int seqOrder;
    private String codeError;

    private String text1;
    private Integer number1;
    private LocalDate date1;
    private String error1;

    private String text2;
    private Integer number2;
    private LocalDate date2;
    private String error2;

    private String text3;
    private Integer number3;
    private LocalDate date3;
    private String error3;

    private String text4;
    private Integer number4;
    private LocalDate date4;
    private String error4;

    private String text5;
    private Integer number5;
    private LocalDate date5;
    private String error5;

    private String text6;
    private Integer number6;
    private LocalDate date6;
    private String error6;

    private String text7;
    private Integer number7;
    private LocalDate date7;
    private String error7;

    private String text8;
    private Integer number8;
    private LocalDate date8;
    private String error8;

    private String text9;
    private Integer number9;
    private LocalDate date9;
    private String error9;

    private String text10;
    private Integer number10;
    private LocalDate date10;
    private String error10;

    public void to(final BaseIndexRow indexRow) {
        indexRow.setSeqOrder(this.seqOrder);
        indexRow.setText1(this.text1);
        indexRow.setNumber1(this.number1);
        indexRow.setDate1(this.date1);
        indexRow.setText2(this.text2);
        indexRow.setNumber2(this.number2);
        indexRow.setDate2(this.date2);
        indexRow.setText3(this.text3);
        indexRow.setNumber3(this.number3);
        indexRow.setDate3(this.date3);
        indexRow.setText4(this.text4);
        indexRow.setNumber4(this.number4);
        indexRow.setDate4(this.date4);
        indexRow.setText5(this.text5);
        indexRow.setNumber5(this.number5);
        indexRow.setDate5(this.date5);
        indexRow.setText6(this.text6);
        indexRow.setNumber6(this.number6);
        indexRow.setDate6(this.date6);
        indexRow.setText7(this.text7);
        indexRow.setNumber7(this.number7);
        indexRow.setDate7(this.date7);
        indexRow.setText8(this.text8);
        indexRow.setNumber8(this.number8);
        indexRow.setDate8(this.date8);
        indexRow.setText9(this.text9);
        indexRow.setNumber9(this.number9);
        indexRow.setDate9(this.date9);
        indexRow.setText10(this.text10);
        indexRow.setNumber10(this.number10);
        indexRow.setDate10(this.date10);
    }

}
