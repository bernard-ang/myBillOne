package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.ContactGroup;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class ContactBasicPayload implements ApiPayload {

    private Integer id;
    private String email;
    private String mobileNo;

    private String text1;
    private Integer number1;
    private LocalDate date1;
    private String text2;
    private Integer number2;
    private LocalDate date2;
    private String text3;
    private Integer number3;
    private LocalDate date3;
    private String text4;
    private Integer number4;
    private LocalDate date4;
    private String text5;
    private Integer number5;
    private LocalDate date5;
    private String text6;
    private Integer number6;
    private LocalDate date6;
    private String text7;
    private Integer number7;
    private LocalDate date7;
    private String text8;
    private Integer number8;
    private LocalDate date8;
    private String text9;
    private Integer number9;
    private LocalDate date9;
    private String text10;
    private Integer number10;
    private LocalDate date10;

    private List<String> groups = new ArrayList<>();


    public static ContactBasicPayload from(Contact contact) {
        ContactBasicPayload payload = new ContactBasicPayload();
        payload.setId(contact.getId());
        payload.setEmail(contact.getEmail());
        payload.setMobileNo(contact.getMobileNo());

        payload.setText1(contact.getText1());
        payload.setDate1(contact.getDate1());
        payload.setNumber1(contact.getNumber1());
        payload.setText2(contact.getText2());
        payload.setDate2(contact.getDate2());
        payload.setNumber2(contact.getNumber2());
        payload.setText3(contact.getText3());
        payload.setDate3(contact.getDate3());
        payload.setNumber3(contact.getNumber3());
        payload.setText4(contact.getText4());
        payload.setDate4(contact.getDate4());
        payload.setNumber4(contact.getNumber4());
        payload.setText5(contact.getText5());
        payload.setDate5(contact.getDate5());
        payload.setNumber5(contact.getNumber5());
        payload.setText6(contact.getText6());
        payload.setDate6(contact.getDate6());
        payload.setNumber6(contact.getNumber6());
        payload.setText7(contact.getText7());
        payload.setDate7(contact.getDate7());
        payload.setNumber7(contact.getNumber7());
        payload.setText8(contact.getText8());
        payload.setDate8(contact.getDate8());
        payload.setNumber8(contact.getNumber8());
        payload.setText9(contact.getText9());
        payload.setDate9(contact.getDate9());
        payload.setNumber9(contact.getNumber9());
        payload.setText10(contact.getText10());
        payload.setDate10(contact.getDate10());
        payload.setNumber10(contact.getNumber10());

        if (contact.getContactGroups() != null) {
            for (ContactGroup contactGroup : contact.getContactGroups()) {
                payload.getGroups().add(contactGroup.getName());
            }
        }

        return payload;
    }

}
