package com.grabbill.server.dto.report;

import com.grabbill.core.model.ProcessStatus;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;


/**
 * @author michaellow
 */
@Data
public class ActivityRecordRow {

    private long typeId;

    private String typeName;

    private long activityId;

    private String activityName;

    private long recordId;

    // BaseRecord.name
    private String email;
    private String mobileNo;

    private ProcessStatus processStatus;
    private String message;

    private boolean emailStatusSent;
    private boolean emailStatusSkipUnsubscribed;
    private boolean emailStatusSkipBounced;
    private boolean emailStatusSoftBounce;
    private boolean emailStatusHardBounce;

    private boolean emailDsnReceivedConfirmation;
    private String emailDsnMessage;

    private Map<String, String> dynamicFields = new HashMap<>();

    private String whatsAppTemplateBody;

    private boolean whatsAppStatusSent;
    private boolean whatsAppStatusSkip;
    private boolean whatsAppStatusDelivered;
    private boolean whatsAppStatusRead;
    private boolean whatsAppStatusAcknowledge;
    private boolean whatsAppStatusFailed;
    private String whatsAppStatusFailedMessage;

}
