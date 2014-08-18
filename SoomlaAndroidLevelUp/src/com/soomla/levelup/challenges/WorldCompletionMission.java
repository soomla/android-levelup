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

package com.soomla.levelup.challenges;

import com.soomla.levelup.gates.WorldCompletionGate;
import com.soomla.rewards.Reward;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A specific type of <code>Mission</code> that has an associated
 * world which opens a <code>WorldCompletionGate</code>.
 * The mission is completed once the world completion gate is opened.
 * <p/>
 * Created by gurdotan on 8/17/14.
 */
public class WorldCompletionMission extends Mission {

    /**
     * Constructor
     *
     * @param id                see parent
     * @param name              see parent
     * @param associatedWorldId the ID of the world to be examined
     */
    public WorldCompletionMission(String id, String name, String associatedWorldId) {
        super(id, name, WorldCompletionGate.class, new Object[]{associatedWorldId});
    }

    /**
     * Constructor
     *
     * @param id                see parent
     * @param name              see parent
     * @param rewards           see parent
     * @param associatedWorldId the ID of the world to be examined
     */
    public WorldCompletionMission(String id, String name, List<Reward> rewards, String associatedWorldId) {
        super(id, name, rewards, WorldCompletionGate.class, new Object[]{associatedWorldId});
    }

    /**
     * @{inheritDoc}
     */
    public WorldCompletionMission(JSONObject jsonMission) throws JSONException {
        super(jsonMission);
    }

}
