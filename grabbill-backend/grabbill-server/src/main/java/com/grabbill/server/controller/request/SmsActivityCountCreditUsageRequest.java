package com.grabbill.server.controller.request;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class SmsActivityCountCreditUsageRequest {

    @Size(max = 255)
    private String smsContent;
    private Integer contactGroupId;
    private List<BaseIndexRowRequest> indexRows = new ArrayList<>();

}
