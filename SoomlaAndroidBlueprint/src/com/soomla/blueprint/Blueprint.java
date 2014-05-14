package com.soomla.blueprint;

import com.soomla.blueprint.scoring.Score;

import java.util.HashMap;

/**
 * Created by refaelos on 06/05/14.
 */
public class Blueprint {
    public static final String DB_KEY_PREFIX = "soomla.blueprint.";

    private HashMap<String, World>   mInitialWorlds;

    public Blueprint(HashMap<String, World> initialWorlds) {
        mInitialWorlds = initialWorlds;
    }

    public Score getScore(String scoreId) {
        return fetchScoreFromWorlds(scoreId, mInitialWorlds);
    }

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
}
