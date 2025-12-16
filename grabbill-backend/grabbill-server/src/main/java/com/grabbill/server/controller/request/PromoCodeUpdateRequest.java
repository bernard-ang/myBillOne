package com.grabbill.server.controller.request;

import com.grabbill.core.entity.PromoCode;
import com.grabbill.core.model.DiscountOccurrence;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * NOTE:
 * Follow Stripe's convention, not able to edit coupon's details other than those less important fields.
 * If need to change discount amount / code, delete and create and new one!
 *
 * @author michaellow
 */
@Data
public class PromoCodeUpdateRequest {

    @NotNull
    @Size(max = 255)
    private String name;

    private OffsetDateTime start;

    private OffsetDateTime end;

    @NotNull
    private DiscountOccurrence discountOccurrence;

    @NotNull
    private Integer discountOccurrenceCount;

    private String remarks;


    public void to(final PromoCode promoCode) {
        promoCode.setName(this.name);
        promoCode.setStart(this.getStart());
        promoCode.setEnd(this.getEnd());
        promoCode.setDiscountOccurrence(this.discountOccurrence);
        promoCode.setDiscountOccurrenceCount(this.discountOccurrenceCount);
        promoCode.setRemarks(this.remarks);
    }

}
