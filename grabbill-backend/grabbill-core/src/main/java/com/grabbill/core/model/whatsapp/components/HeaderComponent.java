package com.grabbill.core.model.whatsapp.components;

import com.grabbill.core.model.whatsapp.request.enums.ComponentFormat;
import com.grabbill.core.model.whatsapp.request.enums.ComponentType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class HeaderComponent extends AbstractComponent {
    private final ComponentType type = ComponentType.HEADER;
    private ComponentFormat format;
}
