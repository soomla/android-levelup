package com.soomla.blueprint;

import com.soomla.blueprint.events.LevelEndedEvent;
import com.soomla.blueprint.events.LevelStartedEvent;
import com.soomla.blueprint.gates.GatesList;
import com.soomla.blueprint.scoring.Score;
import com.soomla.blueprint.scoring.VirtualItemScore;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.exceptions.VirtualItemNotFoundException;

import java.util.HashMap;

/**
 * Created by refaelos on 07/05/14.
 */
public class Level extends World {
    private static String TAG = "SOOMLA Level";

    private int mTimesStarted;
    private int mTimesPlayed;
    private double mLowestDuration = Double.MAX_VALUE;
    private double mHighestDuration = Double.MIN_VALUE;

    private long mStartTime;

    public Level(String worldId) {
        super(worldId);
    }

    public Level(String worldId, GatesList gates, HashMap<String, Score> scores) {
        super(worldId, gates, new HashMap<String, World>(), scores);
    }

    public Level(String worldId, GatesList gates, HashMap<String, World> innerWorlds, HashMap<String, Score> scores) {
        super(worldId, gates, innerWorlds, scores);
    }

    public int getTimesStarted() {
        return mTimesStarted;
    }

    public int getTimesPlayed() {
        return mTimesPlayed;
    }

    public double getLowestDuration() {
        return mLowestDuration;
    }

    public double getHighestDuration() {
        return mHighestDuration;
    }

    public void decScore(String scoreId, double amount) {
        mScores.get(scoreId).dec(amount);
    }

    public void incScore(String scoreId, double amount) {
        mScores.get(scoreId).inc(amount);
    }

    public boolean start() {
        StoreUtils.LogDebug(TAG, "Starting level with worldId: " + getWorldId());

        if (!canStart()) {
            return false;
        }

        mTimesStarted++;

        mStartTime = System.currentTimeMillis();

        BusProvider.getInstance().post(new LevelStartedEvent(this));
        return true;
    }

    public void end() {
        mTimesPlayed++;

        long endTime = System.currentTimeMillis();
        double duration = (endTime-mStartTime) / 1000.0;

        if (duration < mLowestDuration) {
            mLowestDuration = duration;
        }

        if (duration > mHighestDuration) {
            mHighestDuration = duration;
        }

        for(Score score : mScores.values()) {

            score.reset(); // resetting scores

            if (score instanceof VirtualItemScore) {
                String associatedItemId = ((VirtualItemScore) score).getAssociatedItemId();
                try {
                    StoreInventory.giveVirtualItem(associatedItemId, (int) score.getScore());
                } catch (VirtualItemNotFoundException e) {
                    StoreUtils.LogError(TAG, "Couldn't find item associated with a given " +
                            "VirtualItemScore. itemId: " + associatedItemId);
                }
            }
        }

        BusProvider.getInstance().post(new LevelEndedEvent(this));
    }
}
