package com.grabbill.core.model.whatsapp.response;

import com.grabbill.core.model.whatsapp.request.enums.ComponentType;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
public class RefreshTemplateResponse {
    private String id;
    private String name;
    private String language;
    private String status;
    private String category;
    private String waTemplateId;
    private String whatsappId;
    private List<RefreshTemplateResponseComponent> component = new ArrayList<>();
    private int noOfParams;
    private boolean isActive;
    private String updatedBy;
    private Date updatedDate;

    public Optional<RefreshTemplateResponseComponent> getBodyComponent() {
        return component.stream()
                .filter(item -> item.getType().equalsIgnoreCase(ComponentType.BODY.toString()))
                .findAny();
    }

    public String getBodyText() {
        Optional<RefreshTemplateResponseComponent> bodyComponent = this.getBodyComponent();
        return bodyComponent.map(RefreshTemplateResponseComponent::getText).orElse(null);
    }

    public Optional<RefreshTemplateResponseComponent> getHeaderComponent() {
        return component.stream()
                .filter(item -> item.getType().equalsIgnoreCase(ComponentType.HEADER.toString()))
                .findAny();
    }

    public Optional<RefreshTemplateResponseComponent> getFooterComponent() {
        return component.stream()
                .filter(item -> item.getType().equalsIgnoreCase(ComponentType.FOOTER.toString()))
                .findAny();
    }

    public String getFooterText() {
        Optional<RefreshTemplateResponseComponent> footerComponent = this.getFooterComponent();
        return footerComponent.map(RefreshTemplateResponseComponent::getText).orElse(null);
    }

    public Optional<RefreshTemplateResponseComponent> getButtonsComponent() {
        return component.stream()
                .filter(item -> item.getType().equalsIgnoreCase(ComponentType.BUTTONS.toString()))
                .findAny();
    }

    public String getButtonText() {
        Optional<RefreshTemplateResponseComponent> button = this.getButtonsComponent();
        return button.map(component -> component.getButtons().get(0).getText()).orElse(null);
    }
}
