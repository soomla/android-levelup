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

import com.soomla.BusProvider;
import com.soomla.SoomlaUtils;
import com.soomla.data.JSONConsts;
import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.store.SoomlaStore;
import com.soomla.store.data.StoreInfo;
import com.soomla.store.domain.PurchasableVirtualItem;
import com.soomla.store.events.MarketPurchaseEvent;
import com.soomla.store.exceptions.VirtualItemNotFoundException;
import com.soomla.store.purchaseTypes.PurchaseWithMarket;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Gate</code> that has an associated
 * market item. The gate opens once the item has been purchased.
 * This gate is useful when you want to allow unlocking of certain levels
 * or worlds only if they are purchased.
 *
 * Created by refaelos on 07/05/14.
 */
public class PurchasableGate extends Gate {


    /**
     * Constructor
     *
     * @param gateId see parent
     * @param associatedItemId the ID of the item which will open the gate once purchased
     */
    public PurchasableGate(String gateId, String associatedItemId) {
        super(gateId);

        this.mAssociatedItemId = associatedItemId;

        // We can't put the registration in the 'open' function b/c we want to make sure this gate
        // "listens" to MarketPurchaseEvents even if the app was closed and opened again.
        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Constructor
     * Generates an instance of <code>PurchasableGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public PurchasableGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(BPJSONConsts.BP_ASSOCITEMID);

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Converts the current <code>PurchasableGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>PurchasableGate</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCITEMID, mAssociatedItemId);
            jsonObject.put(JSONConsts.SOOM_CLASSNAME, getClass().getSimpleName());
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Attempts to open the gate by purchasing the associated item
     */
    @Override
    public boolean tryOpenInner() {
        try {
            PurchasableVirtualItem pvi = (PurchasableVirtualItem) StoreInfo.getVirtualItem(mAssociatedItemId);
            PurchaseWithMarket ptype = (PurchaseWithMarket) pvi.getPurchaseType();
            SoomlaStore.getInstance().buyWithMarket(ptype.getMarketItem(), getGateId());
            return true;
        } catch (VirtualItemNotFoundException e) {
            SoomlaUtils.LogError(TAG, "The item needed for purchase doesn't exist. itemId: " +
                    mAssociatedItemId);
        } catch (ClassCastException e) {
            SoomlaUtils.LogError(TAG, "The associated item is not a purchasable item. itemId: " +
                    mAssociatedItemId);
        }

        return false;
    }

    @Override
    public boolean canOpen() {
        return true;
    }

    /**
     * Handle market purchases and opens the gate.
     *
     * @param marketPurchaseEvent
     */
    @Subscribe
    public void onMarketPurchaseEvent(MarketPurchaseEvent marketPurchaseEvent) {
        if (marketPurchaseEvent.getPayload().equals(getGateId())) {
            BusProvider.getInstance().unregister(this);
            forceOpen(true);
        }
    }


    /** Private Members */

    private static String TAG = "SOOMLA PurchasableGate";

    private String mAssociatedItemId;
}
