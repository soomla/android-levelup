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
 * A specific type of <code>Mission</code> that has an associated
 * score and a desired record. The mission is completed
 * once the player achieves the desired record for the given score.
 *
 * Created by refaelos on 13/05/14.
 */
public class RecordMission extends Mission {

    /**
     * Constructor
     *
     * @param name see parent
     * @param missionId see parent
     * @param associatedScoreId the ID of the score which is examined
     * @param desiredRecord the record which will complete this mission
     */
    public RecordMission(String name, String missionId, String associatedScoreId, double desiredRecord) {
        super(name, missionId);
        mAssociatedScoreId = associatedScoreId;
        mDesiredRecord = desiredRecord;

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Constructor
     *
     * @param missionId see parent
     * @param name see parent
     * @param rewards see parent
     * @param desiredRecord the record which will complete this mission
     */
    public RecordMission(String missionId, String name, List<Reward> rewards, double desiredRecord) {
        super(missionId, name, rewards);
        mDesiredRecord = desiredRecord;

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Constructor
     * Generates an instance of <code>RecordMission</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public RecordMission(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedScoreId = jsonObject.getString(BPJSONConsts.BP_ASSOCSCOREID);
        mDesiredRecord = jsonObject.getInt(BPJSONConsts.BP_DESIRED_RECORD);

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Converts the current <code>RecordMission</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>RecordMission</code>.
     */
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

    /**
     * Handles changes in score records and completes
     * the mission if the desired record was reached.
     *
     * @param scoreRecordChangedEvent
     */
    @Subscribe
    public void onScoreRecordChanged(ScoreRecordChangedEvent scoreRecordChangedEvent) {
        if (scoreRecordChangedEvent.getScore().getScoreId().equals(mAssociatedScoreId) &&
                scoreRecordChangedEvent.getScore().hasRecordReached(mDesiredRecord)) {
            BusProvider.getInstance().unregister(this);
            setCompleted(true);
        }
    }


    /** Setters and Getters */

    public String getAssociatedScoreId() {
        return mAssociatedScoreId;
    }

    public double getDesiredRecord() {
        return mDesiredRecord;
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA RecordMission";

    private String mAssociatedScoreId;
    private double mDesiredRecord;
}
