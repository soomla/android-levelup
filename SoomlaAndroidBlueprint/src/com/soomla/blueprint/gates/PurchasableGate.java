package com.soomla.blueprint.gates;

import com.soomla.store.BusProvider;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.events.MarketPurchaseEvent;
import com.soomla.store.exceptions.InsufficientFundsException;
import com.soomla.store.exceptions.VirtualItemNotFoundException;
import com.squareup.otto.Subscribe;

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

    @Override
    public void tryOpenInner() {
        try {
            StoreInventory.buy(mAssociatedItemId);
        } catch (InsufficientFundsException e) {
            StoreUtils.LogError(TAG, "Not enough funds to purchase this gate.  gateId: + " +
                    getGateId() + "  itemId: " + mAssociatedItemId);
        } catch (VirtualItemNotFoundException e) {
            StoreUtils.LogError(TAG, "The item needed for purchase doesn't exist. itemId: " +
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
