package com.soomla.blueprint.rewards;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.exceptions.VirtualItemNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Reward</code> that has an associated
 * virtual item.  The user is given this virtual item when the reward
 * is granted.  For example: a user can earn a life reward (<code>VirtualItemReward</code>)
 * which rewards the user with one life (<code>SingleUseVG</code>).
 *
 * Created by refaelos on 13/05/14.
 */
public class VirtualItemReward extends Reward {

    /**
     * Constructor
     *
     * @param rewardId see parent
     * @param name see parent
     * @param amount the amount to give of the associated item when the reward is given
     * @param associatedItemId the ID of the virtual item associated with this reward
     */
    protected VirtualItemReward(String rewardId, String name, int amount, String associatedItemId) {
        super(rewardId, name);
        mAmount = amount;
        mAssociatedItemId = associatedItemId;
    }

    /**
     * Constructor.
     * Generates an instance of <code>VirtualItemReward</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>VirtualItemReward</code>.
     * @throws JSONException
     */
    public VirtualItemReward(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(BPJSONConsts.BP_ASSOCITEMID);
        mAmount = jsonObject.getInt(BPJSONConsts.BP_REWARD_AMOUNT);
    }

    /**
     * Converts the current <code>VirtualItemReward</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>VirtualItemReward</code>.
     */
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

    /**
     * Gives the user the item associated with this reward.
     *
     * @return <code>true</code> if the item was given successfully,
     * <code>false</code> otherwise
     */
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


    /** Setters and Getters **/

    public int getAmount() {
        return mAmount;
    }

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA Reward";

    private int mAmount;
    private String mAssociatedItemId;
}
