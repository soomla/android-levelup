package com.soomla.blueprint.rewards;

/**
 * Created by refaelos on 13/05/14.
 */
public class BadgeReward extends Reward {
    private String mIconUrl;

    protected BadgeReward(String rewardId, String name) {
        super(rewardId, name);
    }

    protected BadgeReward(String rewardId, String name, String iconUrl) {
        super(rewardId, name);
        mIconUrl = iconUrl;
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
