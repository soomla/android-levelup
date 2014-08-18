/*
 * Copyright (C) 2012-2014 Soomla Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soomla.levelup;

import com.soomla.levelup.challenges.Mission;
import com.soomla.levelup.gates.Gate;
import com.soomla.levelup.scoring.Score;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * A level is specific type of <code>World</code> which can be started
 * and ended. During the level's game play, certain parameters are tracked and
 * saved such as level duration, score and number of times the level is played.
 * <p/>
 * Created by refaelos on 07/05/14.
 */
public class Level extends World {

    public enum State {
        Idle,
        Running,
        Paused,
        Ended,
        Completed
    }

    /**
     * Constructor
     *
     * @param id the world's ID
     */
    public Level(String id) {
        super(id);
    }

    /**
     * Constructor
     *
     * @param worldId     see parent
     * @param gate        see parent
     * @param innerWorlds see parent
     * @param scores      see parent
     * @param missions    see parent
     */
    public Level(String worldId, Gate gate, HashMap<String, World> innerWorlds, HashMap<String, Score> scores, List<Mission> missions) {
        super(worldId, gate, innerWorlds, scores, missions);
    }

    /**
     * Constructor.
     * Generates an instance of <code>Level</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Level</code>.
     * @throws JSONException
     */
    public Level(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    /**
     * Private Members *
     */

    private static String TAG = "SOOMLA Level";

    private long mStartTime;
    private long mElapsed;

    private State mState = State.Idle;
}
