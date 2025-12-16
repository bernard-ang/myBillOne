package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.PromoCode;
import com.grabbill.core.model.DiscountOccurrence;
import com.grabbill.core.model.DiscountType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class PromoCodeBasicPayload implements ApiPayload {

    private Integer id;

    private String name;

    private String code;

    private OffsetDateTime start;

    private OffsetDateTime end;

    private Integer discount;

    private DiscountType discountType;

    private DiscountOccurrence discountOccurrence;

    private Integer discountOccurrenceCount;

    private boolean active;

    private String remarks;

    private OffsetDateTime createdTime;

    private OffsetDateTime lastModifiedTime;


    public static PromoCodeBasicPayload from(
            final PromoCode promoCode
    ) {
        PromoCodeBasicPayload payload = new PromoCodeBasicPayload();

        payload.setId(promoCode.getId());
        payload.setName(promoCode.getName());
        payload.setCode(promoCode.getCode());
        payload.setStart(promoCode.getStart());
        payload.setEnd(promoCode.getEnd());
        payload.setDiscount(promoCode.getDiscount());
        payload.setDiscountType(promoCode.getDiscountType());
        payload.setDiscountOccurrence(promoCode.getDiscountOccurrence());
        payload.setDiscountOccurrenceCount(promoCode.getDiscountOccurrenceCount());
        payload.setCreatedTime(promoCode.getCreatedDate());
        payload.setLastModifiedTime(promoCode.getLastModifiedDate());
        payload.setActive(promoCode.isActive());
        payload.setRemarks(promoCode.getRemarks());

        return payload;
    }

}
