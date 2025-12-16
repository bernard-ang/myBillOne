package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTTransactionalEmailActivity;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class MTTransactionalEmailActivityBasicPayload extends BaseActivityBasicPayload {

    private int total;
    private int sent;
    private int opened;
    private int bounced;
    private int bouncedSkip;
    private int unsubscribedSkip;


    public static MTTransactionalEmailActivityBasicPayload from(
            final MTTransactionalEmailActivity mtTransactionalEmailActivity
    ) {
        MTTransactionalEmailActivityBasicPayload instance = new MTTransactionalEmailActivityBasicPayload();
        instance.setId(mtTransactionalEmailActivity.getId());
        instance.setNoOfFiles(mtTransactionalEmailActivity.getMtTransactionalEmailFiles().size());
        instance.copyFrom(mtTransactionalEmailActivity);

        instance.setTotal(mtTransactionalEmailActivity.getMtTransactionalEmailIndexRows().size());
        instance.setSent(mtTransactionalEmailActivity.getEmailStatusSent());
        instance.setOpened(mtTransactionalEmailActivity.getEmailStatusOpened());
        instance.setBounced(mtTransactionalEmailActivity.getEmailStatusBounced());
        instance.setBouncedSkip(mtTransactionalEmailActivity.getEmailStatusBouncedSkip() == null ? 0 : mtTransactionalEmailActivity.getEmailStatusBouncedSkip());
        instance.setUnsubscribedSkip(mtTransactionalEmailActivity.getEmailStatusUnsubscribedSkip());

        return instance;
    }

}
