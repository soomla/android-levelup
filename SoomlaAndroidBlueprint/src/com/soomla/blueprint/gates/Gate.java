package com.soomla.blueprint.gates;

import java.util.List;

/**
 * Created by refaelos on 06/05/14.
 */
public abstract class Gate {
    private GatesList mGatesAfter;
    protected boolean mOpen;
    public abstract List<Gate> canPass();
}
