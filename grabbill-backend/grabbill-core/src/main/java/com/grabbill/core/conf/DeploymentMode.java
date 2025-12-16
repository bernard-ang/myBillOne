package com.grabbill.core.conf;

/**
 * @author michaellow
 */
public enum DeploymentMode {

    SAAS("saas"),

    ON_PREMISE("on-premise");

    private String propValue;

    DeploymentMode(final String propValue) {
        this.propValue = propValue;
    }

    public String getPropValue() {
        return propValue;
    }

}
