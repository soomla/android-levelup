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

import com.soomla.SoomlaUtils;
import com.soomla.levelup.LevelUp;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.levelup.events.ScoreRecordChangedEvent;
import com.soomla.levelup.scoring.Score;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Gate</code> that has an associated
 * score and a desired record. The gate opens
 * once the player achieves the desired record for the given score.
 * <p/>
 * Created by refaelos on 07/05/14.
 */
public class RecordGate extends Gate {


    /**
     * Constructor
     *
     * @param id            see parent
     * @param scoreId       the ID of the score which is examined by this gate
     * @param desiredRecord the record which will open this gate
     */
    public RecordGate(String id, String scoreId, double desiredRecord) {
        super(id);
        this.mAssociatedScoreId = scoreId;
        this.mDesiredRecord = desiredRecord;
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
        mAssociatedScoreId = jsonObject.getString(LUJSONConsts.LU_ASSOCSCOREID);
        mDesiredRecord = jsonObject.getInt(LUJSONConsts.LU_DESIRED_RECORD);
    }

    /**
     * Converts the current <code>RecordGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>RecordGate</code>.
     */
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(LUJSONConsts.LU_ASSOCSCOREID, mAssociatedScoreId);
            jsonObject.put(LUJSONConsts.LU_DESIRED_RECORD, mDesiredRecord);
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
    protected boolean canOpenInner() {
        Score score = LevelUp.getInstance().getScore(mAssociatedScoreId);
        if (score == null) {
            SoomlaUtils.LogError(TAG, "(canOpenInner) couldn't find score with scoreId: " + mAssociatedScoreId);
            return false;
        }

        return score.hasRecordReached(mDesiredRecord);
    }

    @Override
    protected boolean openInner() {
        if (canOpen()) {

            // There's nothing to do here... If the DesiredRecord was reached then the gate is just open.

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
        Score score = scoreRecordChangedEvent.Score;
        if (score.getID() == mAssociatedScoreId && score.hasRecordReached(mDesiredRecord)) {

            // We were thinking what will happen if the score's record will be broken over and over again.
            // It might have made this function being called over and over again.
            // It won't be called b/c ForceOpen(true) calls 'unregisterEvents' inside.
            forceOpen(true);
        }
    }


    /**
     * Private Members
     */

    private static String TAG = "SOOMLA RecordGate";

    private String mAssociatedScoreId;
    private double mDesiredRecord;
}
