package com.soomla.blueprint.challenges;


import com.soomla.blueprint.events.ScoreRecordChangedEvent;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by refaelos on 13/05/14.
 */
public class RecordMission extends Mission {
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
