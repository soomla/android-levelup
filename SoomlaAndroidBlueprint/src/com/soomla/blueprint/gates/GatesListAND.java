package com.soomla.blueprint.gates;


import com.soomla.blueprint.Blueprint;

import java.util.List;

/**
 * Created by refaelos on 07/05/14.
 */
public class GatesListAND extends GatesList {

    public GatesListAND(String gateId) {
        super(gateId);
    }

    public GatesListAND(String gateId, Gate singleGate) {
        super(gateId, singleGate);
    }

    public GatesListAND(String gateId, List<Gate> gates) {
        super(gateId, gates);
    }

    @Override
    public boolean isOpen() {
        for (Gate gate : mGates) {
            if (!gate.isOpen()) {
                return false;
            }
        }
        return true;
    }
}
