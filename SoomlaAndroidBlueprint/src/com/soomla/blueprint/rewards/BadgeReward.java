package com.soomla.blueprint.rewards;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 13/05/14.
 */
public class BadgeReward extends Reward {
    private static final String TAG = "SOOMLA BadgeReward";
    private String mIconUrl;

    protected BadgeReward(String rewardId, String name) {
        super(rewardId, name);
    }

    protected BadgeReward(String rewardId, String name, String iconUrl) {
        super(rewardId, name);
        mIconUrl = iconUrl;
    }

    public BadgeReward(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mIconUrl = jsonObject.getString(BPJSONConsts.BP_REWARD_ICONURL);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_REWARD_ICONURL, mIconUrl);
            jsonObject.put(BPJSONConsts.BP_TYPE, "badge");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
    }

    @Override
    protected boolean giveInner() {
        // nothing to do here... the parent Reward gives in storage
        return true;
    }
}
