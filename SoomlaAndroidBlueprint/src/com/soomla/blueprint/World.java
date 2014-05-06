package com.soomla.blueprint;

import com.soomla.blueprint.gates.GatesList;

import java.util.List;

/**
 * Created by refaelos on 06/05/14.
 */
public class World {
    private String mWorldId;
    private GatesList mGates;
    private List<World> mInnerWorlds;
    private boolean mCompleted;

    public boolean isCompleted() {
        return mCompleted;
    }

    public void setCompleted(boolean mCompleted) {
        this.mCompleted = mCompleted;
    }
}
