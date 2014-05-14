package com.soomla.blueprint;

import com.soomla.blueprint.challenges.Challenge;
import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.LevelsStorage;
import com.soomla.blueprint.events.LevelEndedEvent;
import com.soomla.blueprint.events.LevelStartedEvent;
import com.soomla.blueprint.gates.GatesList;
import com.soomla.blueprint.scoring.Score;
import com.soomla.blueprint.scoring.VirtualItemScore;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreInventory;
import com.soomla.store.StoreUtils;
import com.soomla.store.exceptions.VirtualItemNotFoundException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by refaelos on 07/05/14.
 */
public class Level extends World {
    private static String TAG = "SOOMLA Level";
    private long mStartTime;

    public Level(String worldId) {
        super(worldId);
    }

    public Level(String worldId, GatesList gates, HashMap<String, Score> scores, List<Challenge> challenges) {
        super(worldId, gates, new HashMap<String, World>(), scores, challenges);
    }

    public Level(String worldId, GatesList gates, HashMap<String, World> innerWorlds, HashMap<String, Score> scores, List<Challenge> challenges) {
        super(worldId, gates, innerWorlds, scores, challenges);
    }

    public Level(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_TYPE, "level");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public int getTimesStarted() {
        return LevelsStorage.getTimesStarted(this);
    }

    public int getTimesPlayed() {
        return LevelsStorage.getTimesPlayed(this);
    }

    public double getSlowestDuration() {
        return LevelsStorage.getSlowestDuration(this);
    }

    public double getFastestDuration() {
        return LevelsStorage.getFastestDuration(this);
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

        LevelsStorage.incTimesStarted(this);

        mStartTime = System.currentTimeMillis();

        BusProvider.getInstance().post(new LevelStartedEvent(this));
        return true;
    }

    public void end() {
        LevelsStorage.incTimesPlayed(this);

        long endTime = System.currentTimeMillis();
        double duration = (endTime-mStartTime) / 1000.0;

        if (duration < getSlowestDuration()) {
            LevelsStorage.setSlowestDuration(this, duration);
        }

        if (duration > getFastestDuration()) {
            LevelsStorage.setFastestDuration(this, duration);
        }

        for(Score score : mScores.values()) {
            if (score instanceof VirtualItemScore) { // giving the user the items he collected
                String associatedItemId = ((VirtualItemScore) score).getAssociatedItemId();
                try {
                    StoreInventory.giveVirtualItem(associatedItemId, (int) score.getTempScore());
                } catch (VirtualItemNotFoundException e) {
                    StoreUtils.LogError(TAG, "Couldn't find item associated with a given " +
                            "VirtualItemScore. itemId: " + associatedItemId);
                }
            }

            score.saveAndReset(); // resetting scores
        }

        BusProvider.getInstance().post(new LevelEndedEvent(this));
    }
}
