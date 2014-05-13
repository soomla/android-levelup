package com.soomla.blueprint;

import com.soomla.blueprint.scoring.Score;

import java.util.HashMap;

/**
 * Created by refaelos on 06/05/14.
 */
public class Blueprint {
    private static HashMap<String, World>   mInitialWorlds;

    public static Score getScore(String scoreId) {
        return fetchScoreFromWorlds(scoreId, mInitialWorlds);
    }

    public static World getWorld(String worldId) {
        return fetchWorld(worldId, mInitialWorlds);
    }

    private static Score fetchScoreFromWorlds(String scoreId, HashMap<String, World> worlds) {
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

    private static World fetchWorld(String worldId, HashMap<String, World> worlds) {
        World retWorld = worlds.get(worldId);
        if (retWorld == null) {
            for (World world : worlds.values()) {
                retWorld = fetchWorld(worldId, world.getInnerWorlds());
            }
        }

        return retWorld;
    }
}
