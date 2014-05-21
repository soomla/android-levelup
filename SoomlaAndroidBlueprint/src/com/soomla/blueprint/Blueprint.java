package com.soomla.blueprint;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.scoring.Score;
import com.soomla.store.StoreUtils;
import com.soomla.store.data.KeyValDatabase;
import com.soomla.store.data.StorageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by refaelos on 06/05/14.
 */
public class Blueprint {
    public static final String DB_KEY_PREFIX = "soomla.blueprint.";

    public void initialize(List<World> initialWorlds) {
        HashMap<String, World> worldMap = new HashMap<String, World>();
        for (World world : initialWorlds) {
            worldMap.put(world.getWorldId(), world);
        }
        mInitialWorlds = worldMap;
        save();
    }

    /**
     * Persists the entire blueprint model to storage.
     */
    public void save() {
        String bp_json = toJSONObject().toString();
        StoreUtils.LogDebug(TAG, "saving Blueprint to DB. json is: " + bp_json);
        String key = DB_KEY_PREFIX + "model";
        StorageManager.getKeyValueStorage().setValue(key, bp_json);
    }

    /**
     * Converts the current <code>Blueprint</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Blueprint</code>.
     */
    private JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray worldsJSON = new JSONArray();
            for (World world : mInitialWorlds.values()) {
                worldsJSON.put(world.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_WORLDS, worldsJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

//    public void load() {}
//    public void loadFromFile(String filePath) {}
//    public void loadFromJSON(String json) {}

    /**
     * Retrieves a score object from the blueprint
     *
     * @param scoreId the ID of the score to get
     * @return
     */
    public Score getScore(String scoreId) {
        return fetchScoreFromWorlds(scoreId, mInitialWorlds);
    }

    /**
     * Retrieves a world object from the blueprint
     *
     * @param worldId the ID of the world to get
     * @return
     */
    public World getWorld(String worldId) {
        return fetchWorld(worldId, mInitialWorlds);
    }

    private Score fetchScoreFromWorlds(String scoreId, HashMap<String, World> worlds) {
        Score retScore = null;
        for (World world : worlds.values()) {
            retScore = world.getScores().get(scoreId);
            if (retScore == null) {
                retScore = fetchScoreFromWorlds(scoreId, world.getInnerWorlds());
            }
            if (retScore != null) {
                break;
            }
        }

        return retScore;
    }

    private World fetchWorld(String worldId, HashMap<String, World> worlds) {
        World retWorld = worlds.get(worldId);
        if (retWorld == null) {
            for (World world : worlds.values()) {
                retWorld = fetchWorld(worldId, world.getInnerWorlds());
            }
        }

        return retWorld;
    }


    /** Singleton **/

    public static Blueprint getInstance() {
        if (sInstance == null) {
            sInstance = new Blueprint();
        }
        return sInstance;
    }

    private Blueprint() {}
    private static Blueprint sInstance;


    /** Private Members **/

    private static final String TAG = "SOOMLA Blueprint";
    private HashMap<String, World>   mInitialWorlds;
}
