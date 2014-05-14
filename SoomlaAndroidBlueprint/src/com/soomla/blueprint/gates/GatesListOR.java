package com.soomla.blueprint.gates;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by refaelos on 07/05/14.
 */
public class GatesListOR extends GatesList {
    private static final String TAG = "SOOMLA GatesListOR";

    public GatesListOR(String gateId) {
        super(gateId);
    }

    public GatesListOR(String gateId, Gate singleGate) {
        super(gateId, singleGate);
    }

    public GatesListOR(String gateId, List<Gate> gates) {
        super(gateId, gates);
    }

    public GatesListOR(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_TYPE, "listOR");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
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
