package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.BasePlanOption;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class PlanOptionPayload implements ApiPayload {

    private Long id;

    private Long size;

    private Double price;

    void copyFrom(final Long id, final BasePlanOption planOption) {
        this.setId(id);
        this.setSize(planOption.getSize());
        this.setPrice(planOption.getPrice());
    }

}
