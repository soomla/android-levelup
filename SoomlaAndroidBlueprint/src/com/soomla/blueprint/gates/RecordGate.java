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

package com.soomla.blueprint.gates;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.events.GateCanBeOpenedEvent;
import com.soomla.blueprint.events.ScoreRecordChangedEvent;
import com.soomla.blueprint.scoring.Score;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;
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
     * {@inheritDoc}
     */
    @Override
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
     * Checks if the gate meets its record criteria for opening.
     *
     * @return <code>true</code> if the score's record has reached
     * the desired value, <code>false</code> otherwise
     */
    private boolean canPass() {
        Score score = Blueprint.getInstance().getScore(mAssociatedScoreId);
        if (score == null) {
            StoreUtils.LogError(TAG, "(isOpen) couldn't find score with scoreId: " + mAssociatedScoreId);
            return false;
        }

        return score.hasRecordReached(mDesiredRecord);
    }

    @Override
    public void tryOpenInner() {
        if (canPass()) {
            forceOpen(true);
        }
    }


    /**
     * Handles changes in score records and notifies if the gate can be opened.
     *
     * @param scoreRecordChangedEvent
     */
    @Subscribe
    public void onScoreRecordChanged(ScoreRecordChangedEvent scoreRecordChangedEvent) {
        if (scoreRecordChangedEvent.getScore().getScoreId().equals(mAssociatedScoreId) &&
                scoreRecordChangedEvent.getScore().hasRecordReached(mDesiredRecord)) {
            BusProvider.getInstance().unregister(this);
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }


    /** Private Members */

    private static String TAG = "SOOMLA GlobalScoresGate";

    private String mAssociatedScoreId;
    private double mDesiredRecord;
}
