package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.EmbeddedLinkClick;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class EmbeddedLinkClickPayload implements ApiPayload {

    private Long id;
    private String email;
    private OffsetDateTime clickedDate;


    public static EmbeddedLinkClickPayload from(EmbeddedLinkClick embeddedLinkClick) {
        EmbeddedLinkClickPayload payload = new EmbeddedLinkClickPayload();
        payload.id = embeddedLinkClick.getId();
        payload.email = embeddedLinkClick.getEmail();
        payload.clickedDate = embeddedLinkClick.getClickedDate();

        return payload;
    }

}
