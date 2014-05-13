package com.soomla.blueprint.challenges;

import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
import com.soomla.store.events.CurrencyBalanceChangedEvent;
import com.soomla.store.events.GoodBalanceChangedEvent;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by refaelos on 13/05/14.
 */
public class BalanceMission extends Mission {
    private String mAssociatedItemId;
    private int mDesiredBalance;

    public BalanceMission(String name, String missionId, String associatedItemId, int desiredBalance) {
        super(name, missionId);
        mAssociatedItemId = associatedItemId;
        mDesiredBalance = desiredBalance;

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public BalanceMission(String missionId, String name, List<Reward> rewards, String associatedItemId, int desiredBalance) {
        super(missionId, name, rewards);
        mAssociatedItemId = associatedItemId;
        mDesiredBalance = desiredBalance;

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }

    public int getDesiredBalance() {
        return mDesiredBalance;
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
            setCompleted(true);
        }
    }
}
