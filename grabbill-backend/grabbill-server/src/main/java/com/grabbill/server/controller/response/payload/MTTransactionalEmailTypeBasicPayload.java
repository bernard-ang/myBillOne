package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTTransactionalEmailActivity;
import com.grabbill.core.entity.MTTransactionalEmailType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class MTTransactionalEmailTypeBasicPayload extends BaseTypeBasicPayload {

    private String lastSentBy;

    private OffsetDateTime lastSentDate;

    private boolean hasAttachment;

    private boolean passwordProtected;

    private boolean archive;

    private boolean autoPurge;

    public static MTTransactionalEmailTypeBasicPayload from (
            final MTTransactionalEmailType mtTransactionalEmailType,
            final List<MTTransactionalEmailActivity> activities
    ) {
        MTTransactionalEmailTypeBasicPayload instance = new MTTransactionalEmailTypeBasicPayload();
        instance.setId(mtTransactionalEmailType.getId());
        instance.setLastSentBy(mtTransactionalEmailType.getLastSentBy());
        instance.setLastSentDate(mtTransactionalEmailType.getLastSentDate());
        instance.setHasAttachment(mtTransactionalEmailType.isHasAttachment());
        instance.setArchive(mtTransactionalEmailType.isArchive());
        instance.setPasswordProtected(mtTransactionalEmailType.isPasswordProtected());
        instance.setAutoPurge(mtTransactionalEmailType.isAutoPurge());
        instance.copyFrom(mtTransactionalEmailType);

        int noOfFiles = 0;
        for (MTTransactionalEmailActivity activity : activities) {
            noOfFiles += activity.getMtTransactionalEmailFiles().size();
        }
        instance.setNoOfFiles(noOfFiles);

        return instance;
    }

}
