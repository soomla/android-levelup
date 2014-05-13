package com.soomla.blueprint;

import com.soomla.blueprint.gates.GatesList;
import com.soomla.blueprint.scoring.Score;
import com.soomla.store.StoreUtils;

import java.util.HashMap;

/**
 * Created by refaelos on 06/05/14.
 */
public class World {
    private static String TAG = "SOOMLA World";
    private String mWorldId;
    private GatesList mGates;
    private HashMap<String, World> mInnerWorldIds;
    private boolean mCompleted;
    protected HashMap<String, Score> mScores;


    public World(String worldId) {
        this.mWorldId = worldId;
        this.mGates = null;
        this.mCompleted = false;
        this.mInnerWorldIds = new HashMap<String, World>();
        this.mScores = new HashMap<String, Score>();
    }

    public World(String worldId, GatesList gates, HashMap<String, World> innerWorlds, HashMap<String, Score> scores) {
        this.mWorldId = worldId;
        this.mInnerWorldIds = innerWorlds;
        this.mScores = scores;
        this.mCompleted = false;
        this.mGates = gates;
    }


    public HashMap<String, Double> getRecordScores() {
        HashMap<String, Double> records = new HashMap<String, Double>();
        for(Score score : mScores.values()) {
            records.put(score.getScoreId(), score.getRecord());
        }

        return records;
    }
    public HashMap<String, Double> getLatestScores() {
        HashMap<String, Double> latest = new HashMap<String, Double>();
        for(Score score : mScores.values()) {
            latest.put(score.getScoreId(), score.getLatest());
        }

        return latest;
    }
    public HashMap<String, Score> getScores() {
        return mScores;
    }
    public void setScore(String scoreId, double scoreVal) {
        Score score = mScores.get(scoreId);
        if (score == null) {
            StoreUtils.LogError(TAG, "(setScore) Can't find scoreId: " + scoreId + "  worldId: " + mWorldId);
            return;
        }
        score.setScore(scoreVal);
    }

    public boolean isCompleted() {
        return mCompleted;
    }
    public void setCompleted(boolean mCompleted) {
        setCompleted(mCompleted, false);

    }
    public void setCompleted(boolean mCompleted, boolean recursive) {
        if (recursive) {
            for (World world : mInnerWorldIds.values()) {
                world.setCompleted(mCompleted, true);
            }
        }
        this.mCompleted = mCompleted;
    }

    public String getWorldId() {
        return mWorldId;
    }

    public GatesList getGates() {
        return mGates;
    }

    public HashMap<String, World> getInnerWorlds() {
        return mInnerWorldIds;
    }

    public boolean canStart() {
        if (mGates == null) {
            return true;
        }
        return mGates.isOpen();
    }
}
