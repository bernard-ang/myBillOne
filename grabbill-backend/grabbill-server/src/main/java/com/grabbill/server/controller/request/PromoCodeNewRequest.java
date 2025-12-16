package com.grabbill.server.controller.request;

import com.grabbill.core.entity.PromoCode;
import com.grabbill.core.model.DiscountOccurrence;
import com.grabbill.core.model.DiscountType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class PromoCodeNewRequest {

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 255)
    private String code;

    private OffsetDateTime start;

    private OffsetDateTime end;

    @NotNull
    private Integer discount;

    @NotNull
    private DiscountType discountType;

    @NotNull
    private DiscountOccurrence discountOccurrence;

    @NotNull
    private Integer discountOccurrenceCount;

    private boolean active;

    private String remarks;


    public void to(final PromoCode promoCode) {
        promoCode.setName(this.name);
        promoCode.setCode(this.code);

        promoCode.setStart(this.getStart());
        promoCode.setEnd(this.getEnd());
        promoCode.setDiscount(this.getDiscount());
        promoCode.setDiscountType(this.getDiscountType());
        promoCode.setDiscountOccurrence(this.discountOccurrence);
        promoCode.setDiscountOccurrenceCount(this.discountOccurrenceCount);

        promoCode.setActive(this.active);
        promoCode.setRemarks(this.remarks);
    }

}
