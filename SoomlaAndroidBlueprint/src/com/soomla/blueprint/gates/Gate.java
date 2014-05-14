package com.soomla.blueprint.gates;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.GatesStorage;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 06/05/14.
 */
public abstract class Gate {
    private static final String TAG = "SOOMLA Gate";
    private String mGateId;

    public Gate(String gateId) {
        this.mGateId = gateId;
    }

    public Gate(JSONObject jsonObject) throws JSONException {
        mGateId = jsonObject.getString(BPJSONConsts.BP_GATE_GATEID);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_GATE_GATEID, mGateId);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
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
