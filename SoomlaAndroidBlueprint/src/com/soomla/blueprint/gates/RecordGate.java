package com.soomla.blueprint.gates;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.events.GateCanBeOpenedEvent;
import com.soomla.blueprint.events.GateOpenedEvent;
import com.soomla.blueprint.events.ScoreRecordChangedEvent;
import com.soomla.blueprint.scoring.Score;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;
import com.squareup.otto.Subscribe;

import java.util.HashMap;

/**
 * Created by refaelos on 07/05/14.
 */
public class RecordGate extends Gate {
    private static String TAG = "SOOMLA GlobalScoresGate";
    private String mAssociatedScoreId;
    private double mDesiredRecord;

    public RecordGate(String gateId, String scoreId, double desiredRecord) {
        super(gateId);
        this.mAssociatedScoreId = scoreId;
        this.mDesiredRecord = desiredRecord;

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    @Override
    public void open() {
        mOpen = true;
        BusProvider.getInstance().post(new GateOpenedEvent(this));
    }

    @Override
    public boolean isOpen() {
        if (!mOpen) {
            Score score = Blueprint.getScore(mAssociatedScoreId);
            if (score == null) {
                StoreUtils.LogError(TAG, "(isOpen) couldn't find score with scoreId: " + mAssociatedScoreId);
                return false;
            }

            if (!score.hasRecordReached(mDesiredRecord)) {
                return false;
            }

            mOpen = true;
        }

        return true;
    }

    @Subscribe
    public void onScoreRecordChanged(ScoreRecordChangedEvent scoreRecordChangedEvent) {
        if (scoreRecordChangedEvent.getScore().getScoreId().equals(mAssociatedScoreId) &&
                scoreRecordChangedEvent.getScore().hasRecordReached(mDesiredRecord)) {
            BusProvider.getInstance().unregister(this);
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }
}
