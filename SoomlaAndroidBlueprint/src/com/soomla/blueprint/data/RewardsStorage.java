package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.events.RewardGivenEvent;
import com.soomla.blueprint.rewards.BadgeReward;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
import com.soomla.store.data.StorageManager;

/**
 * Created by refaelos on 13/05/14.
 */
public class RewardsStorage {

    private static String keyRewards(String rewardId, String postfix) {
        return Blueprint.DB_KEY_PREFIX + "rewards." + rewardId + "." + postfix;
    }

    private static String keyRewardGiven(String rewardId) {
        return keyRewards(rewardId, "given");
    }

    private static String keyRewardIdxSeqGiven(String rewardId) {
        return keyRewards(rewardId, "seq.idx");
    }


    /** Badges **/

    public static void setRewardStatus(Reward reward, boolean give) {
        setRewardStatus(reward, give, true);
    }

    public static void setRewardStatus(Reward reward, boolean give, boolean notify) {
        String rewardId = reward.getRewardId();
        String key = keyRewardGiven(rewardId);

        if (give) {
            StorageManager.getKeyValueStorage().setValue(key, "yes");

            if (notify) {
                BusProvider.getInstance().post(new RewardGivenEvent(reward));
            }
        } else {
            StorageManager.getKeyValueStorage().deleteKeyValue(key);
        }
    }

    public static boolean isRewardGiven(Reward reward) {
        String rewardId = reward.getRewardId();
        String key = keyRewardGiven(rewardId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val != null;
    }


    /** Sequence Reward **/

    public static int getLastSeqIdxGiven(Reward reward) {
        String rewardId = reward.getRewardId();
        String key = keyRewardIdxSeqGiven(rewardId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        if (val == null) {
            return -1;
        }
        return Integer.parseInt(val);
    }

    public static void setLastSeqIdxGiven(Reward reward, int idx) {
        String rewardId = reward.getRewardId();
        String key = keyRewardIdxSeqGiven(rewardId);

        StorageManager.getKeyValueStorage().setValue(key, String.valueOf(idx));
    }
}
