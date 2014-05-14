package com.soomla.blueprint.rewards;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.exceptions.VirtualItemNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 13/05/14.
 */
public class VirtualItemReward extends Reward {
    private static final String TAG = "SOOMLA Reward";
    private int mAmount;
    private String mAssociatedItemId;

    protected VirtualItemReward(String rewardId, String name, int amount, String associatedItemId) {
        super(rewardId, name);
        mAmount = amount;
        mAssociatedItemId = associatedItemId;
    }

    public VirtualItemReward(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(BPJSONConsts.BP_ASSOCITEMID);
        mAmount = jsonObject.getInt(BPJSONConsts.BP_REWARD_AMOUNT);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCITEMID, mAssociatedItemId);
            jsonObject.put(BPJSONConsts.BP_REWARD_AMOUNT, mAmount);
            jsonObject.put(BPJSONConsts.BP_TYPE, "item");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public int getAmount() {
        return mAmount;
    }

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }

    @Override
    public boolean giveInner() {
        try {
            StoreInventory.giveVirtualItem(mAssociatedItemId, mAmount);
        } catch (VirtualItemNotFoundException e) {
            StoreUtils.LogError(TAG, "(give) Couldn't find associated itemId: " + mAssociatedItemId);
            return false;
        }
        return true;
    }
}
