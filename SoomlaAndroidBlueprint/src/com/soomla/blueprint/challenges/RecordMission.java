package com.soomla.blueprint.challenges;


import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.events.ScoreRecordChangedEvent;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by refaelos on 13/05/14.
 */
public class RecordMission extends Mission {
    private static final String TAG = "SOOMLA RecordMission";
    private String mAssociatedScoreId;
    private double mDesiredRecord;

    public RecordMission(String name, String missionId, String associatedScoreId, double desiredRecord) {
        super(name, missionId);
        mAssociatedScoreId = associatedScoreId;
        mDesiredRecord = desiredRecord;

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public RecordMission(String missionId, String name, List<Reward> rewards, double desiredRecord) {
        super(missionId, name, rewards);
        mDesiredRecord = desiredRecord;

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public RecordMission(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedScoreId = jsonObject.getString(BPJSONConsts.BP_ASSOCSCOREID);
        mDesiredRecord = jsonObject.getInt(BPJSONConsts.BP_DESIRED_RECORD);

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCSCOREID, mAssociatedScoreId);
            jsonObject.put(BPJSONConsts.BP_DESIRED_RECORD, mDesiredRecord);
            jsonObject.put(BPJSONConsts.BP_TYPE, "record");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public String getAssociatedScoreId() {
        return mAssociatedScoreId;
    }

    public double getDesiredRecord() {
        return mDesiredRecord;
    }

    @Subscribe
    public void onScoreRecordChanged(ScoreRecordChangedEvent scoreRecordChangedEvent) {
        if (scoreRecordChangedEvent.getScore().getScoreId().equals(mAssociatedScoreId) &&
                scoreRecordChangedEvent.getScore().hasRecordReached(mDesiredRecord)) {
            BusProvider.getInstance().unregister(this);
            setCompleted(true);
        }
    }
}
