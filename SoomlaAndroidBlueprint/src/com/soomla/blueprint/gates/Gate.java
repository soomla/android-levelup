package com.soomla.blueprint.gates;

/**
 * Created by refaelos on 06/05/14.
 */
public abstract class Gate {
    private String mGateId;
    protected boolean mOpen;

    public Gate(String gateId) {
        this.mGateId = gateId;
    }

    public abstract void open();

    public String getGateId() {
        return mGateId;
    }

    public boolean isOpen() {
        return mOpen;
    }
}
