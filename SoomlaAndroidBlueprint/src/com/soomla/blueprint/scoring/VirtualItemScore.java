package com.soomla.blueprint.scoring;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 06/05/14.
 */
public class VirtualItemScore extends Score {
    private static final String TAG = "SOOMLA VirtualItemScore";
    private String mAssociatedItemId;

    public VirtualItemScore(String scoreId, String name, String associatedItemId) {
        super(scoreId, name);
        this.mAssociatedItemId = associatedItemId;
    }

    public VirtualItemScore(String scoreId, String name, boolean higherBetter, String associatedItemId) {
        super(scoreId, name, higherBetter);
        this.mAssociatedItemId = associatedItemId;
    }

    public VirtualItemScore(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(BPJSONConsts.BP_ASSOCITEMID);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCITEMID, mAssociatedItemId);
            jsonObject.put(BPJSONConsts.BP_TYPE, "item");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }
}
