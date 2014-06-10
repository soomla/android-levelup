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

package com.soomla.levelup.gates;

import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.data.GateStorage;
import com.soomla.levelup.events.GateCanBeOpenedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.events.CurrencyBalanceChangedEvent;
import com.soomla.store.events.GoodBalanceChangedEvent;
import com.soomla.store.exceptions.VirtualItemNotFoundException;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Gate</code> that has an associated
 * virtual item and a desired balance. The gate opens once
 * the item's balance reaches the desired balance.
 *
 * Created by refaelos on 07/05/14.
 */
public class BalanceGate extends Gate {
    private static String TAG = "SOOMLA BalanceGate";
    private String mAssociatedItemId;
    private int mDesiredBalance;

    /**
     * Constructor
     *
     * @param gateId see parent
     * @param associatedItemId the ID of the item who's balance is examined
     * @param desiredBalance the balance which will open this gate
     */
    public BalanceGate(String gateId, String associatedItemId, int desiredBalance) {
        super(gateId);
        this.mDesiredBalance = desiredBalance;
        this.mAssociatedItemId = associatedItemId;

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Constructor
     * Generates an instance of <code>BalanceGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public BalanceGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(BPJSONConsts.BP_ASSOCITEMID);
        mDesiredBalance = jsonObject.getInt(BPJSONConsts.BP_DESIRED_BALANCE);

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Converts the current <code>RecordGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>RecordGate</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCITEMID, mAssociatedItemId);
            jsonObject.put(BPJSONConsts.BP_DESIRED_BALANCE, mDesiredBalance);
            jsonObject.put(BPJSONConsts.BP_TYPE, "balance");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Checks if the gate meets its item balance criteria for opening.
     *
     * @return <code>true</code> if the item's balance has
     * reached the desired balance, <code>false</code> otherwise
     */
    private boolean canPass() {
        if (GateStorage.isOpen(this)) {
            return true;
        }
        try {
            if (StoreInventory.getVirtualItemBalance(mAssociatedItemId) < mDesiredBalance) {
                return false;
            }
        } catch (VirtualItemNotFoundException e) {
            StoreUtils.LogError(TAG, "(canPass) Couldn't find itemId. itemId: " + mAssociatedItemId);
            return false;
        }
        return true;
    }

    @Override
    public void tryOpenInner() {
        if (canPass()) {
            try {
                StoreInventory.takeVirtualItem(mAssociatedItemId, mDesiredBalance);
            } catch (VirtualItemNotFoundException e) {
                StoreUtils.LogError(TAG, "(open) Couldn't find itemId. itemId: " + mAssociatedItemId);
                return;
            }

            forceOpen(true);
        }
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
            BusProvider.getInstance().unregister(this);
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }
}
