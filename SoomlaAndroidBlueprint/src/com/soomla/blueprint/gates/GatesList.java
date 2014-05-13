package com.soomla.blueprint.gates;

import com.soomla.blueprint.Blueprint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by refaelos on 06/05/14.
 */
public abstract class GatesList extends Gate {

    protected List<Gate> mGates;

    public GatesList(String gateId) {
        super(gateId);
        mGates = new ArrayList<Gate>();
    }

    public GatesList(String gateId, Gate singleGate) {
        super(gateId);
        mGates = new ArrayList<Gate>();
        mGates.add(singleGate);
    }

    public GatesList(String gateId, List<Gate> gates) {
        super(gateId);
        mGates = gates;
    }

    public void addGate(Gate gate) {
        mGates.add(gate);
    }

    @Override
    public abstract boolean isOpen();

    public int size() {
        return mGates.size();
    }

    @Override
    public void open() {
        for (Gate gate : mGates) {
            gate.open();
        }
    }

    public List<Gate> getGates() {
        return mGates;
    }
}
