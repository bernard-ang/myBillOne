package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.CreditsPlanOption;
import com.grabbill.core.model.CreditType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class SmsCreditsPlanOptionsPayload implements ApiPayload {

    private List<SmsCreditsPlanOptionPayload> options = new ArrayList<>();


    public static SmsCreditsPlanOptionsPayload from (final List<CreditsPlanOption> dataObjects) {
        SmsCreditsPlanOptionsPayload payload = new SmsCreditsPlanOptionsPayload();
        for (CreditsPlanOption dataObject : dataObjects) {
            payload.getOptions().add(SmsCreditsPlanOptionPayload.from(dataObject));
        }

        return payload;
    }

    @Data
    public static class SmsCreditsPlanOptionPayload {

        private Integer id;
        private CreditType type;
        private String name;
        private String description;
        private Integer quantity;
        private Double price;

        public static SmsCreditsPlanOptionPayload from (final CreditsPlanOption dataObject) {
            SmsCreditsPlanOptionPayload payload = new SmsCreditsPlanOptionPayload();
            payload.setId(dataObject.getId());
            payload.setName(dataObject.getName());
            payload.setType(dataObject.getType());
            payload.setDescription(dataObject.getDescription());
            payload.setQuantity(dataObject.getQuantity());
            payload.setPrice(dataObject.getPrice());

            return payload;
        }
    }

}
