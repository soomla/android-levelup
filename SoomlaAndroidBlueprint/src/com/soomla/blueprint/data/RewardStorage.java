/*
 * Copyright (C) 2012-2014 Soomla Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.events.RewardGivenEvent;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
import com.soomla.store.data.StorageManager;

/**
 * A utility class for persisting and querying the state of rewards.
 * Use this class to check if a certain reward was given, or to
 * set its state.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 *
 * Created by refaelos on 13/05/14.
 */
public class RewardStorage {

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

    /**
     * Sets the reward status of the given reward\
     *
     * @param reward the reward to set status
     * @param give <code>true</code>
     */
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

    /**
     * Checks whether the given reward was given.
     *
     * @param reward the reward to check
     * @return <code>true</code> if the reward was already given,
     * <code>false</code> otherwise
     */
    public static boolean isRewardGiven(Reward reward) {
        String rewardId = reward.getRewardId();
        String key = keyRewardGiven(rewardId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val != null;
    }


    /** Sequence Reward **/

    /**
     * Retrieves the index of the last reward given in a sequence of rewards.
     *
     * @param reward the reward to check
     * @return the index of the reward in the sequence
     */
    public static int getLastSeqIdxGiven(Reward reward) {
        String rewardId = reward.getRewardId();
        String key = keyRewardIdxSeqGiven(rewardId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        if (val == null) {
            return -1;
        }
        return Integer.parseInt(val);
    }

    /**
     * Sets the index of the last reward given in a sequence of rewards.
     *
     * @param reward the reward who's index is to be set
     * @param idx the index to set
     */
    public static void setLastSeqIdxGiven(Reward reward, int idx) {
        String rewardId = reward.getRewardId();
        String key = keyRewardIdxSeqGiven(rewardId);

        StorageManager.getKeyValueStorage().setValue(key, String.valueOf(idx));
    }
}
