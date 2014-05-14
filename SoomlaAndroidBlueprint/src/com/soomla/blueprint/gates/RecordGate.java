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

    public boolean canPass() {
        Score score = Blueprint.getScore(mAssociatedScoreId);
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

    @Subscribe
    public void onScoreRecordChanged(ScoreRecordChangedEvent scoreRecordChangedEvent) {
        if (scoreRecordChangedEvent.getScore().getScoreId().equals(mAssociatedScoreId) &&
                scoreRecordChangedEvent.getScore().hasRecordReached(mDesiredRecord)) {
            BusProvider.getInstance().unregister(this);
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }
}
