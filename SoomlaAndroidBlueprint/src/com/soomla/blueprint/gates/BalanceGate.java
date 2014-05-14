package com.soomla.blueprint.gates;

import com.soomla.blueprint.data.GatesStorage;
import com.soomla.blueprint.events.GateCanBeOpenedEvent;
import com.soomla.blueprint.events.GateOpenedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.events.CurrencyBalanceChangedEvent;
import com.soomla.store.events.GoodBalanceChangedEvent;
import com.soomla.store.exceptions.VirtualItemNotFoundException;
import com.squareup.otto.Subscribe;

/**
 * Created by refaelos on 07/05/14.
 */
public class BalanceGate extends Gate {
    private static String TAG = "SOOMLA BalanceGate";
    private String mAssociatedItemId;
    private int mDesiredBalance;

    public BalanceGate(String gateId, String associatedItemId, int desiredBalance) {
        super(gateId);
        this.mDesiredBalance = desiredBalance;
        this.mAssociatedItemId = associatedItemId;

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    public boolean canPass() {
        if (GatesStorage.isOpen(this)) {
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

    @Subscribe
    public void onCurrencyBalanceChanged(CurrencyBalanceChangedEvent currencyBalanceChangedEvent) {
        checkItemIdBalance(currencyBalanceChangedEvent.getCurrency().getItemId(), currencyBalanceChangedEvent.getBalance());
    }

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
