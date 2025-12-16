package com.grabbill.core.service;

/**
 * @author michaellow
 */
public class TaxServiceImpl implements TaxService {

    // TODO: load this from DB?
    private static final double SST_RATE = 0;

    @Override
    public double getSstRate() {
        return SST_RATE;
    }

}
