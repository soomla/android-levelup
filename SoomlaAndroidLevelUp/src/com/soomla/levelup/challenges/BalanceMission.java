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

package com.soomla.levelup.challenges;

import com.soomla.SoomlaUtils;
import com.soomla.data.JSONConsts;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.rewards.Reward;
import com.soomla.store.events.CurrencyBalanceChangedEvent;
import com.soomla.store.events.GoodBalanceChangedEvent;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A specific type of <code>Mission</code> that has an associated
 * virtual item and a desired balance. The mission is completed
 * once the item's balance reaches the desired balance.
 *
 * Created by refaelos on 13/05/14.
 */
public class BalanceMission extends Mission {


    /**
     * Constructor
     *
     * @param name see parent
     * @param missionId see parent
     * @param associatedItemId the ID of the item who's balance is examined
     * @param desiredBalance the balance which will complete this mission
     */
    public BalanceMission(String name, String missionId, String associatedItemId, int desiredBalance) {
        super(missionId, name);
        mAssociatedItemId = associatedItemId;
        mDesiredBalance = desiredBalance;
    }

    /**
     * Constructor
     *
     * @param missionId see parent
     * @param name see parent
     * @param rewards see parent
     * @param associatedItemId the ID of the item who's balance is examined
     * @param desiredBalance the balance which will complete this mission
     */
    public BalanceMission(String missionId, String name, List<Reward> rewards, String associatedItemId, int desiredBalance) {
        super(missionId, name, rewards);
        mAssociatedItemId = associatedItemId;
        mDesiredBalance = desiredBalance;
    }

    /**
     * Constructor
     * Generates an instance of <code>BalanceMission</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public BalanceMission(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(LUJSONConsts.LU_ASSOCITEMID);
        mDesiredBalance = jsonObject.getInt(LUJSONConsts.LU_DESIRED_BALANCE);
    }

    /**
     * Converts the current <code>BalanceMission</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>BalanceMission</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(LUJSONConsts.LU_ASSOCITEMID, mAssociatedItemId);
            jsonObject.put(LUJSONConsts.LU_DESIRED_BALANCE, mDesiredBalance);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Handles changes in the associated item's balance (if it's a currency)
     *
     * @param currencyBalanceChangedEvent
     */
    @Subscribe
    public void onCurrencyBalanceChanged(CurrencyBalanceChangedEvent currencyBalanceChangedEvent) {
        checkItemIdBalance(currencyBalanceChangedEvent.getCurrency().getItemId(), currencyBalanceChangedEvent.getBalance());
    }

    /**
     * Handles changes in the associated item's balance (if it's a virtual good)
     *
     * @param goodBalanceChangedEvent
     */
    @Subscribe
    public void onGoodBalanceChanged(GoodBalanceChangedEvent goodBalanceChangedEvent) {
        checkItemIdBalance(goodBalanceChangedEvent.getGood().getItemId(), goodBalanceChangedEvent.getBalance());
    }

    private void checkItemIdBalance(String itemId, int balance) {
        if (itemId.equals(mAssociatedItemId) && balance >= mDesiredBalance) {
            setCompleted(true);
        }
    }


    /** Setters and Getters */

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }

    public int getDesiredBalance() {
        return mDesiredBalance;
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA BalanceMission";

    private String mAssociatedItemId;
    private int mDesiredBalance;

}
