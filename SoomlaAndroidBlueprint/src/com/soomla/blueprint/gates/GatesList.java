package com.soomla.blueprint.gates;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by refaelos on 06/05/14.
 */
public abstract class GatesList extends Gate {
    private static final String TAG = "SOOMLA GatesList";
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

    public GatesList(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mGates = new ArrayList<Gate>();
        JSONArray gatesArr = jsonObject.getJSONArray(BPJSONConsts.BP_GATES);
        for (int i=0; i<gatesArr.length(); i++) {
            JSONObject gateJSON = gatesArr.getJSONObject(i);
            String type = gateJSON.getString(BPJSONConsts.BP_TYPE);
            if (type.equals("balance")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("listAND")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("listOR")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("record")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("purchasable")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("worldCompletion")) {
                mGates.add(new BalanceGate(gateJSON));
            } else {
                StoreUtils.LogError(TAG, "Unknown gate type: " + type);
            }
        }
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            JSONArray gatesArr = new JSONArray();
            for (Gate gate : mGates) {
                gatesArr.put(gate.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_GATES, gatesArr);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
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
    public void tryOpenInner() {
        for (Gate gate : mGates) {
            gate.tryOpen();
        }
    }

    public List<Gate> getGates() {
        return mGates;
    }
}
