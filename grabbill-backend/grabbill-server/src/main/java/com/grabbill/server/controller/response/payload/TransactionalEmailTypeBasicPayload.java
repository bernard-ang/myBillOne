package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class TransactionalEmailTypeBasicPayload extends BaseTypeBasicPayload {

    private String lastSentBy;

    private OffsetDateTime lastSentDate;

    private boolean hasAttachment;

    private boolean passwordProtected;

    private boolean archive;

    private boolean autoPurge;

    public static TransactionalEmailTypeBasicPayload from (
            final TransactionalEmailType transactionalEmailType,
            final List<TransactionalEmailActivity> activities
    ) {
        TransactionalEmailTypeBasicPayload instance = new TransactionalEmailTypeBasicPayload();
        instance.setId(transactionalEmailType.getId());
        instance.setLastSentBy(transactionalEmailType.getLastSentBy());
        instance.setLastSentDate(transactionalEmailType.getLastSentDate());
        instance.setHasAttachment(transactionalEmailType.isHasAttachment());
        instance.setArchive(transactionalEmailType.isArchive());
        instance.setPasswordProtected(transactionalEmailType.isPasswordProtected());
        instance.setAutoPurge(transactionalEmailType.isAutoPurge());
        instance.copyFrom(transactionalEmailType);

        int noOfFiles = 0;
        for (TransactionalEmailActivity activity : activities) {
            noOfFiles += activity.getTransactionalEmailFiles().size();
        }
        instance.setNoOfFiles(noOfFiles);

        return instance;
    }

}
