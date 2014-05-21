package com.soomla.blueprint.gates;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreController;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.data.StoreInfo;
import com.soomla.store.domain.PurchasableVirtualItem;
import com.soomla.store.events.MarketPurchaseEvent;
import com.soomla.store.exceptions.InsufficientFundsException;
import com.soomla.store.exceptions.VirtualItemNotFoundException;
import com.soomla.store.purchaseTypes.PurchaseWithMarket;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 07/05/14.
 */
public class PurchasableGate extends Gate {
    private static String TAG = "SOOMLA PurchasableGate";
    private String mAssociatedItemId;

    public PurchasableGate(String gateId, String associatedItemId) {
        super(gateId);

        this.mAssociatedItemId = associatedItemId;

        // We can't put the registration in the 'open' function b/c we want to make sure this gate
        // "listens" to MarketPurchaseEvents even if the app was closed and opened again.
        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    public PurchasableGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(BPJSONConsts.BP_ASSOCITEMID);

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCITEMID, mAssociatedItemId);
            jsonObject.put(BPJSONConsts.BP_TYPE, "purchasable");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    @Override
    public void tryOpenInner() {
        try {
            PurchasableVirtualItem pvi = (PurchasableVirtualItem) StoreInfo.getVirtualItem(mAssociatedItemId);
            PurchaseWithMarket ptype = (PurchaseWithMarket) pvi.getPurchaseType();
            StoreController.getInstance().buyWithMarket(ptype.getMarketItem(), getGateId());
        } catch (VirtualItemNotFoundException e) {
            StoreUtils.LogError(TAG, "The item needed for purchase doesn't exist. itemId: " +
                    mAssociatedItemId);
        } catch (ClassCastException e) {
            StoreUtils.LogError(TAG, "The associated item is not a purchasable item. itemId: " +
                    mAssociatedItemId);
        }
    }

    @Subscribe
    public void onMarketPurchaseEvent(MarketPurchaseEvent marketPurchaseEvent) {
        if (marketPurchaseEvent.getPayload().equals(getGateId())) {
            BusProvider.getInstance().unregister(this);
            forceOpen(true);
        }
    }
}
