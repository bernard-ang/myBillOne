package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.TransactionalEmailActivity;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class TransactionalEmailActivityBasicPayload extends BaseActivityBasicPayload {

    private int total;
    private int sent;
    private int opened;
    private int bounced;
    private int bouncedSkip;
    private int unsubscribedSkip;


    public static TransactionalEmailActivityBasicPayload from(
            final TransactionalEmailActivity transactionalEmailActivity
    ) {
        TransactionalEmailActivityBasicPayload instance = new TransactionalEmailActivityBasicPayload();
        instance.setId(transactionalEmailActivity.getId());
        instance.setNoOfFiles(transactionalEmailActivity.getTransactionalEmailFiles().size());
        instance.copyFrom(transactionalEmailActivity);

        instance.setTotal(transactionalEmailActivity.getTransactionalEmailIndexRows().size());
        instance.setSent(transactionalEmailActivity.getEmailStatusSent());
        instance.setOpened(transactionalEmailActivity.getEmailStatusOpened());
        instance.setBounced(transactionalEmailActivity.getEmailStatusBounced());
        instance.setBouncedSkip(transactionalEmailActivity.getEmailStatusBouncedSkip() == null ? 0 : transactionalEmailActivity.getEmailStatusBouncedSkip());
        instance.setUnsubscribedSkip(transactionalEmailActivity.getEmailStatusUnsubscribedSkip());

        return instance;
    }

}
