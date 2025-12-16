package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.BaseActivityService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.payload.JobPayload;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author michaellow
 */
public class JobControllerServiceImpl implements JobControllerService {

    @Autowired
    @Qualifier("digitalFilingActivityService")
    private BaseActivityService<DigitalFilingType, DigitalFilingActivity> dfaService;

    @Autowired
    @Qualifier("transactionalEmailActivityService")
    private BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> txeaService;

    @Autowired
    @Qualifier("emailCampaignActivityService")
    private BaseActivityService<EmailCampaignType, EmailCampaignActivity> ecaService;

    @Autowired
    @Qualifier("whatsAppActivityService")
    private BaseActivityService<WhatsAppType, WhatsAppActivity> whatsAppActivityService;

    @Autowired
    @Qualifier("smsActivityService")
    private BaseActivityService<SmsType, SmsActivity> smsActivityService;


    @Override
    public BaseActivity getActivity(final Job targetJob) {
        BaseActivity activity = null;
        if (DomainType.DIGITAL_FILING.equals(targetJob.getDomainType())) {
            activity = dfaService.getById(targetJob.getActivityId()).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB2003,
                            "Digital Filing Activity with ID [" + targetJob.getActivityId() + "] is not found!"
                    )
            );


        } else if (DomainType.TRANSACTIONAL_EMAIL.equals(targetJob.getDomainType())) {
            activity = txeaService.getById(targetJob.getActivityId()).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB3003,
                            "Transactional Email Activity with ID [" + targetJob.getActivityId() + "] is not found!"
                    )
            );

        } else if (DomainType.EMAIL_CAMPAIGN.equals(targetJob.getDomainType())) {

            activity = ecaService.getById(targetJob.getActivityId()).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB4003,
                            "Email Campaign Activity with ID [" + targetJob.getActivityId() + "] is not found!"
                    )
            );

        } else if (DomainType.WHATSAPP.equals(targetJob.getDomainType())) {

            activity = whatsAppActivityService.getById(targetJob.getActivityId()).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB10003,
                            "WhatsApp Activity with ID [" + targetJob.getActivityId() + "] is not found!"
                    )
            );
        } else if (DomainType.SMS.equals(targetJob.getDomainType())) {

            activity = smsActivityService.getById(targetJob.getActivityId()).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB10003,
                            "SMS Activity with ID [" + targetJob.getActivityId() + "] is not found!"
                    )
            );
        }

        return activity;
    }

    @Override
    public void copyToPayload(final JobPayload jobPayload, final BaseActivity activity) {
        if (activity instanceof DigitalFilingActivity) {
            jobPayload.setDigitalFiling(JobPayload.DigitalFiling.from((DigitalFilingActivity) activity));

        } else if (activity instanceof TransactionalEmailActivity) {
            jobPayload.setTransactionalEmail(JobPayload.TransactionalEmail.from((TransactionalEmailActivity) activity));

        } else if(activity instanceof EmailCampaignActivity) {
            jobPayload.setEmailCampaign(JobPayload.EmailCampaign.from((EmailCampaignActivity) activity));

        } else if(activity instanceof SmsActivity) {
            jobPayload.setSms(JobPayload.Sms.from((SmsActivity) activity));

        } else if(activity instanceof WhatsAppActivity) {
            jobPayload.setWhatsApp(JobPayload.WhatsApp.from((WhatsAppActivity) activity));
        }
    }

}
