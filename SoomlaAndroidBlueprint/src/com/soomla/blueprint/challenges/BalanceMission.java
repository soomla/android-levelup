package com.soomla.blueprint.challenges;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;
import com.soomla.store.events.CurrencyBalanceChangedEvent;
import com.soomla.store.events.GoodBalanceChangedEvent;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by refaelos on 13/05/14.
 */
public class BalanceMission extends Mission {
    private static final String TAG = "SOOMLA BalanceMission";
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

    public BalanceMission(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(BPJSONConsts.BP_ASSOCITEMID);
        mDesiredBalance = jsonObject.getInt(BPJSONConsts.BP_DESIRED_BALANCE);

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

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
