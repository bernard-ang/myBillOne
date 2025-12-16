package com.grabbill.core.model.whatsapp.components;

import com.grabbill.core.model.whatsapp.request.enums.ComponentType;
import lombok.Data;

@Data
public abstract class AbstractComponent {
    private ComponentType type;
}
