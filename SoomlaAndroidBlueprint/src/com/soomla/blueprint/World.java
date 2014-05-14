package com.soomla.blueprint;

import com.soomla.blueprint.challenges.Challenge;
import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.WorldsStorage;
import com.soomla.blueprint.gates.GatesList;
import com.soomla.blueprint.gates.GatesListAND;
import com.soomla.blueprint.gates.GatesListOR;
import com.soomla.blueprint.scoring.RangeScore;
import com.soomla.blueprint.scoring.Score;
import com.soomla.blueprint.scoring.VirtualItemScore;
import com.soomla.store.StoreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by refaelos on 06/05/14.
 */
public class World {
    private static String TAG = "SOOMLA World";
    private String mWorldId;
    private GatesList mGates;
    private HashMap<String, World> mInnerWorldIds;
    protected HashMap<String, Score> mScores;
    private List<Challenge> mChallenges;

    public World(String worldId) {
        this.mWorldId = worldId;
        this.mGates = null;
        this.mInnerWorldIds = new HashMap<String, World>();
        this.mScores = new HashMap<String, Score>();
        this.mChallenges = null;
    }

    public World(String worldId, GatesList gates, HashMap<String, World> innerWorlds, HashMap<String, Score> scores, List<Challenge> challenges) {
        this.mWorldId = worldId;
        this.mInnerWorldIds = innerWorlds;
        this.mScores = scores;
        this.mGates = gates;
        mChallenges = challenges;
    }

    public World(JSONObject jsonObject) throws JSONException {

        mWorldId = jsonObject.getString(BPJSONConsts.BP_WORLD_WORLDID);

        mInnerWorldIds = new HashMap<String, World>();
        JSONArray worldsArr = jsonObject.getJSONArray(BPJSONConsts.BP_WORLDS);
        for (int i=0; i<worldsArr.length(); i++) {
            JSONObject worldJSON = worldsArr.getJSONObject(i);
            String type = worldJSON.getString(BPJSONConsts.BP_TYPE);
            if (type.equals("world")) {
                World world = new World(worldJSON);
                mInnerWorldIds.put(world.getWorldId(), world);
            } else if (type.equals("level")) {
                Level level = new Level(worldJSON);
                mInnerWorldIds.put(level.getWorldId(), level);
            } else {
                StoreUtils.LogError(TAG, "Unknown world type: " + type);
            }
        }

        mScores = new HashMap<String, Score>();
        JSONArray scoresArr = jsonObject.getJSONArray(BPJSONConsts.BP_SCORES);
        for (int i=0; i<scoresArr.length(); i++) {
            JSONObject scoreJSON = scoresArr.getJSONObject(i);
            String type = scoreJSON.getString(BPJSONConsts.BP_TYPE);
            if (type.equals("range")) {
                Score score = new RangeScore(scoreJSON);
                mScores.put(score.getScoreId(), score);
            } else if (type.equals("item")) {
                Score score = new VirtualItemScore(scoreJSON);
                mScores.put(score.getScoreId(), score);
            } else {
                StoreUtils.LogError(TAG, "Unknown score type: " + type);
            }
        }

        mChallenges = new ArrayList<Challenge>();
        JSONArray challengesArr = jsonObject.getJSONArray(BPJSONConsts.BP_CHALLENGES);
        for (int i=0; i<challengesArr.length(); i++) {
            JSONObject challengesJSON = challengesArr.getJSONObject(i);
            mChallenges.add(new Challenge(challengesJSON));
        }

        JSONObject gateListJSON = jsonObject.getJSONObject(BPJSONConsts.BP_GATES);
        String type = gateListJSON.getString(BPJSONConsts.BP_TYPE);
        if (type.equals("listOR")) {
            mGates = new GatesListOR(gateListJSON);
        } else if (type.equals("listAND")) {
            mGates = new GatesListAND(gateListJSON);
        } else {
            StoreUtils.LogError(TAG, "Unknown gates-list type: " + type);
        }
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_WORLD_WORLDID, mWorldId);
            jsonObject.put(BPJSONConsts.BP_GATES, mGates.toJSONObject());

            JSONArray worldsArr = new JSONArray();
            for (World world : mInnerWorldIds.values()) {
                worldsArr.put(world.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_WORLDS, worldsArr);

            JSONArray scoresArr = new JSONArray();
            for (Score score : mScores.values()) {
                scoresArr.put(score.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_SCORES, scoresArr);

            JSONArray challengesArr = new JSONArray();
            for (Challenge challenge : mChallenges) {
                challengesArr.put(challenge.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_CHALLENGES, challengesArr);

        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public List<Challenge> getChallenges() {
        return mChallenges;
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
        score.setTempScore(scoreVal);
    }

    public boolean isCompleted() {
        return WorldsStorage.isCompleted(this);
    }
    public void setCompleted(boolean mCompleted) {
        setCompleted(mCompleted, false);

    }
    public void setCompleted(boolean completed, boolean recursive) {
        if (recursive) {
            for (World world : mInnerWorldIds.values()) {
                world.setCompleted(completed, true);
            }
        }
        WorldsStorage.setCompleted(this, completed);
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
        return mGates == null || mGates.isOpen();
    }
}
