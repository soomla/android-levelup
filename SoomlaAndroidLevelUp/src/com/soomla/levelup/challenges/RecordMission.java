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

package com.soomla.levelup.challenges;


import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.events.ScoreRecordChangedEvent;
import com.soomla.levelup.rewards.Reward;
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

    public static final String TYPE_NAME = "record";

    /**
     * Constructor
     *  @param missionId see parent
     * @param name see parent
     * @param associatedScoreId the ID of the score which is examined
     * @param desiredRecord the record which will complete this mission
     */
    public RecordMission(String missionId, String name, String associatedScoreId, double desiredRecord) {
        super(missionId, name);
        mAssociatedScoreId = associatedScoreId;
        mDesiredRecord = desiredRecord;
    }

    /**
     * Constructor
     *
     * @param missionId see parent
     * @param name see parent
     * @param rewards see parent
     * @param desiredRecord the record which will complete this mission
     */
    public RecordMission(String missionId, String name, List<Reward> rewards, String associatedScoreId, double desiredRecord) {
        super(missionId, name, rewards);
        mAssociatedScoreId = associatedScoreId;
        mDesiredRecord = desiredRecord;
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
            jsonObject.put(BPJSONConsts.BP_TYPE, TYPE_NAME);
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
        if (scoreRecordChangedEvent.Score.getScoreId().equals(mAssociatedScoreId) &&
                scoreRecordChangedEvent.Score.hasRecordReached(mDesiredRecord)) {
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
