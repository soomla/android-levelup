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
import com.soomla.levelup.LevelUp;
import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.events.ScoreRecordChangedEvent;
import com.soomla.levelup.scoring.Score;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Gate</code> that has an associated
 * score and a desired record. The gate opens
 * once the player achieves the desired record for the given score.
 *
 * Created by refaelos on 07/05/14.
 */
public class RecordGate extends Gate {


    /**
     * Constructor
     *
     * @param gateId see parent
     * @param scoreId the ID of the score which is examined by this gate
     * @param desiredRecord the record which will open this gate
     */
    public RecordGate(String gateId, String scoreId, double desiredRecord) {
        super(gateId);
        this.mAssociatedScoreId = scoreId;
        this.mDesiredRecord = desiredRecord;

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Constructor
     * Generates an instance of <code>RecordGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public RecordGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedScoreId = jsonObject.getString(BPJSONConsts.BP_ASSOCSCOREID);
        mDesiredRecord = jsonObject.getInt(BPJSONConsts.BP_DESIRED_RECORD);

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Converts the current <code>RecordGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>RecordGate</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCSCOREID, mAssociatedScoreId);
            jsonObject.put(BPJSONConsts.BP_DESIRED_RECORD, mDesiredRecord);
            jsonObject.put(JSONConsts.SOOM_CLASSNAME, getClass().getSimpleName());
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Checks if the gate meets its record criteria for opening.
     *
     * @return <code>true</code> if the score's record has reached
     * the desired value, <code>false</code> otherwise
     */
    @Override
    public boolean canOpen() {
        Score score = LevelUp.getInstance().getScore(mAssociatedScoreId);
        if (score == null) {
            SoomlaUtils.LogError(TAG, "(canPass) couldn't find score with scoreId: " + mAssociatedScoreId);
            return false;
        }

//        return score.hasTempReached(mDesiredRecord);
        return score.hasRecordReached(mDesiredRecord);
    }

    @Override
    public boolean tryOpenInner() {
        if (canOpen()) {
            forceOpen(true);
            return true;
        }

        return false;
    }


    /**
     * Handles changes in score records and notifies if the gate can be opened.
     *
     * @param scoreRecordChangedEvent
     */
    @Subscribe
    public void onScoreRecordChanged(ScoreRecordChangedEvent scoreRecordChangedEvent) {
        if (scoreRecordChangedEvent.Score.getScoreId().equals(mAssociatedScoreId) &&
                scoreRecordChangedEvent.Score.hasRecordReached(mDesiredRecord)) {
            BusProvider.getInstance().unregister(this);
            // gate can now open
        }
    }


    /** Private Members */

    private static String TAG = "SOOMLA RecordGate";

    private String mAssociatedScoreId;
    private double mDesiredRecord;
}
