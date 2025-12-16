package com.grabbill.core.conf;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * @author michaellow
 */
@Getter
@ConfigurationProperties("deployment")
public class DeploymentProperties {

    private final DeploymentMode mode;

    @ConstructorBinding
    public DeploymentProperties(
            final String mode
    ) {
        if (DeploymentMode.ON_PREMISE.getPropValue().equalsIgnoreCase(mode)) {
            this.mode = DeploymentMode.ON_PREMISE;
        } else {
            this.mode = DeploymentMode.SAAS;
        }
    }

    public boolean isOnPremise() {
        return DeploymentMode.ON_PREMISE.equals(mode);
    }

}
