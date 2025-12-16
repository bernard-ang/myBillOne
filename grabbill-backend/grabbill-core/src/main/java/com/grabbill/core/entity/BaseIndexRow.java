package com.grabbill.core.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;

/**
 * @author michaellow
 */
@Data
@MappedSuperclass
public class BaseIndexRow {

    @Column(name = "seq_order")
    private int seqOrder;

    @Column(name = "text1")
    private String text1;

    @Column(name = "number1")
    private Integer number1;

    @Column(name = "date1")
    private LocalDate date1;

    @Column(name = "text2")
    private String text2;

    @Column(name = "number2")
    private Integer number2;

    @Column(name = "date2")
    private LocalDate date2;

    @Column(name = "text3")
    private String text3;

    @Column(name = "number3")
    private Integer number3;

    @Column(name = "date3")
    private LocalDate date3;

    @Column(name = "text4")
    private String text4;

    @Column(name = "number4")
    private Integer number4;

    @Column(name = "date4")
    private LocalDate date4;

    @Column(name = "text5")
    private String text5;

    @Column(name = "number5")
    private Integer number5;

    @Column(name = "date5")
    private LocalDate date5;

    @Column(name = "text6")
    private String text6;

    @Column(name = "number6")
    private Integer number6;

    @Column(name = "date6")
    private LocalDate date6;

    @Column(name = "text7")
    private String text7;

    @Column(name = "number7")
    private Integer number7;

    @Column(name = "date7")
    private LocalDate date7;

    @Column(name = "text8")
    private String text8;

    @Column(name = "number8")
    private Integer number8;

    @Column(name = "date8")
    private LocalDate date8;

    @Column(name = "text9")
    private String text9;

    @Column(name = "number9")
    private Integer number9;

    @Column(name = "date9")
    private LocalDate date9;

    @Column(name = "text10")
    private String text10;

    @Column(name = "number10")
    private Integer number10;

    @Column(name = "date10")
    private LocalDate date10;

}
