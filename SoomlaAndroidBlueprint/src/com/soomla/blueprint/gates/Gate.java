package com.soomla.blueprint.gates;

import com.soomla.blueprint.data.GatesStorage;

/**
 * Created by refaelos on 06/05/14.
 */
public abstract class Gate {
    private String mGateId;

    public Gate(String gateId) {
        this.mGateId = gateId;
    }

    public void tryOpen() {
        if (GatesStorage.isOpen(this)) {
            return;
        }

        tryOpenInner();
    }

    protected abstract void tryOpenInner();

    public void forceOpen(boolean open) {
        GatesStorage.setOpen(this, open);
    }

    public String getGateId() {
        return mGateId;
    }

    public boolean isOpen() {
        return GatesStorage.isOpen(this);
    }
}
