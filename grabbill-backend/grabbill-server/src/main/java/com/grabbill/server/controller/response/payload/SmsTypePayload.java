package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.ContactGroup;
import com.grabbill.core.entity.SmsActivity;
import com.grabbill.core.entity.SmsIndexField;
import com.grabbill.core.entity.SmsType;
import com.grabbill.core.model.SmsFieldType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class SmsTypePayload extends BaseTypePayload {

    private String smsFrom;
    private String smsContent;
    private SmsFieldType smsFieldType;
    private int totalSms;
    private int smsStatusSent;
    private int smsStatusError;
    private int totalCreditUsed;
    private int totalActivities;

    private Integer contactGroupId;
    private String contactGroupName;


    public static SmsTypePayload from(
            final SmsType smsType,
            final List<SmsActivity> smsActivities
    ) {
        SmsTypePayload instance = new SmsTypePayload();
        instance.setId(smsType.getId());
        instance.copyFrom(smsType);
        instance.setSmsFrom(smsType.getSmsFrom());
        instance.setSmsContent(smsType.getSmsContent());
        instance.setSmsFieldType(smsType.getSmsFieldType());

        int totalActivities = 0;
        int totalSms = 0;
        int smsStatusError = 0;
        int smsStatusSent = 0;
        int smsCreditUsed = 0;

        if (smsActivities != null && !smsActivities.isEmpty()) {
            for (SmsActivity emailCampaignActivity : smsActivities) {
                totalSms += emailCampaignActivity.getTotalSms();
                smsStatusError += emailCampaignActivity.getSmsStatusError();
                smsStatusSent += emailCampaignActivity.getSmsStatusSent();
                smsCreditUsed += emailCampaignActivity.getSmsCreditUsed();
            }
            totalActivities = smsActivities.size();
        }

        instance.setTotalSms(totalSms);
        instance.setSmsStatusError(smsStatusError);
        instance.setSmsStatusSent(smsStatusSent);
        instance.setTotalCreditUsed(smsCreditUsed);
        instance.setTotalActivities(totalActivities);

        List<BaseIndexFieldPayload> indexFields = new ArrayList<>();
        for (SmsIndexField smsIndexField : smsType.getSmsIndexFields()) {
            BaseIndexFieldPayload indexFieldPayload = new BaseIndexFieldPayload();
            indexFieldPayload.setId(smsIndexField.getId());
            indexFieldPayload.copyFrom(smsIndexField);
            indexFields.add(indexFieldPayload);
        }
        instance.setIndexFields(indexFields);

        ContactGroup contactGroup = smsType.getContactGroup();
        if (contactGroup != null) {
            instance.setContactGroupId(contactGroup.getId());
            instance.setContactGroupName(contactGroup.getName());
        }

        return instance;
    }

}
