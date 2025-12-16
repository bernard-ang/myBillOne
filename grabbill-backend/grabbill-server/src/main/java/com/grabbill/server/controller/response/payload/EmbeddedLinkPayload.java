package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.EmbeddedLink;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class EmbeddedLinkPayload implements ApiPayload {

    private Long id;

    private String url;

    private int totalClicks;
    private int totalUniqueClicks;


    public static EmbeddedLinkPayload from(final EmbeddedLink embeddedLink, int totalClicks, int totalUniqueClicks) {
        EmbeddedLinkPayload instance = new EmbeddedLinkPayload();
        instance.id = embeddedLink.getId();
        instance.url = embeddedLink.getUrl();
        instance.totalClicks = totalClicks;
        instance.totalUniqueClicks = totalUniqueClicks;

        return instance;
    }

}
