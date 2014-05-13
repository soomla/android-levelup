package com.soomla.blueprint.rewards;

import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.exceptions.VirtualItemNotFoundException;

/**
 * Created by refaelos on 13/05/14.
 */
public class VirtualItemReward extends Reward {
    private static final String TAG = "SOOMLA Reward";
    private int mAmount;
    private String mAssociatedItemId;

    public int getAmount() {
        return mAmount;
    }

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }

    @Override
    public void give() {
        try {
            StoreInventory.giveVirtualItem(mAssociatedItemId, mAmount);
        } catch (VirtualItemNotFoundException e) {
            StoreUtils.LogError(TAG, "(give) Couldn't find associated itemId: " + mAssociatedItemId);
        }
    }
}
