package com.grabbill.server.controller.request;

import com.grabbill.core.entity.Contact;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class ContactRequest {

    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String mobileNo;

    @Size(max = 255)
    private String text1;
    private Integer number1;
    private LocalDate date1;

    @Size(max = 255)
    private String text2;
    private Integer number2;
    private LocalDate date2;

    @Size(max = 255)
    private String text3;
    private Integer number3;
    private LocalDate date3;

    @Size(max = 255)
    private String text4;
    private Integer number4;
    private LocalDate date4;

    @Size(max = 255)
    private String text5;
    private Integer number5;
    private LocalDate date5;

    @Size(max = 255)
    private String text6;
    private Integer number6;
    private LocalDate date6;

    @Size(max = 255)
    private String text7;
    private Integer number7;
    private LocalDate date7;

    @Size(max = 255)
    private String text8;
    private Integer number8;
    private LocalDate date8;

    @Size(max = 255)
    private String text9;
    private Integer number9;
    private LocalDate date9;

    @Size(max = 255)
    private String text10;
    private Integer number10;
    private LocalDate date10;

    private List<Integer> groups = new ArrayList<>();

    public void to(final Contact contact) {
        contact.setEmail(this.getEmail());
        contact.setMobileNo(this.getMobileNo());
        contact.setText1(this.getText1());
        contact.setDate1(this.getDate1());
        contact.setNumber1(this.getNumber1());
        contact.setText2(this.getText2());
        contact.setDate2(this.getDate2());
        contact.setNumber2(this.getNumber2());
        contact.setText3(this.getText3());
        contact.setDate3(this.getDate3());
        contact.setNumber3(this.getNumber3());
        contact.setText4(this.getText4());
        contact.setDate4(this.getDate4());
        contact.setNumber4(this.getNumber4());
        contact.setText5(this.getText5());
        contact.setDate5(this.getDate5());
        contact.setNumber5(this.getNumber5());
        contact.setText6(this.getText6());
        contact.setDate6(this.getDate6());
        contact.setNumber6(this.getNumber6());
        contact.setText7(this.getText7());
        contact.setDate7(this.getDate7());
        contact.setNumber7(this.getNumber7());
        contact.setText8(this.getText8());
        contact.setDate8(this.getDate8());
        contact.setNumber8(this.getNumber8());
        contact.setText9(this.getText9());
        contact.setDate9(this.getDate9());
        contact.setNumber9(this.getNumber9());
        contact.setText10(this.getText10());
        contact.setDate10(this.getDate10());
        contact.setNumber10(this.getNumber10());
    }

}
