package com.soomla.blueprint.gates;


import com.soomla.blueprint.Blueprint;

import java.util.List;

/**
 * Created by refaelos on 07/05/14.
 */
public class GatesListOR extends GatesList {

    public GatesListOR(String gateId) {
        super(gateId);
    }

    public GatesListOR(String gateId, Gate singleGate) {
        super(gateId, singleGate);
    }

    public GatesListOR(String gateId, List<Gate> gates) {
        super(gateId, gates);
    }


    @Override
    public boolean isOpen() {
        for (Gate gate : mGates) {
            if (gate.isOpen()) {
                return true;
            }
        }
        return false;
    }
}
