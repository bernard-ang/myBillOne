package com.grabbill.core.model.plan;

/**
 * @author michaellow
 */
public enum PlanType {

    STORAGE("sto"),
    TRANSACTION_EMAIL("txe"),
    MARKETING_EMAIL("ec");

    private String name;

    PlanType(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
