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

package com.soomla.levelup.gates;

import com.soomla.BusProvider;
import com.soomla.SoomlaUtils;
import com.soomla.levelup.LevelUp;
import com.soomla.levelup.World;
import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.events.WorldCompletedEvent;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Gate</code> that has an associated
 * world. The gate opens once the world has been completed.
 *
 * Created by refaelos on 07/05/14.
 */
public class WorldCompletionGate extends Gate {

    public static final String TYPE_NAME = "worldCompletion";

    /**
     * Constructor
     *
     * @param gateId see parent
     * @param associatedWorldId the ID of the world which needs to be completed
     */
    public WorldCompletionGate(String gateId, String associatedWorldId) {
        super(gateId);
        this.mAssociatedWorldId = associatedWorldId;

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Constructor
     * Generates an instance of <code>WorldCompletionGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public WorldCompletionGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedWorldId = jsonObject.getString(BPJSONConsts.BP_ASSOCWORLDID);

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Converts the current <code>WorldCompletionGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>WorldCompletionGate</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCWORLDID, mAssociatedWorldId);
            jsonObject.put(BPJSONConsts.BP_TYPE, TYPE_NAME);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Checks if the gate meets its world completion criteria for opening.
     *
     * @return <code>true</code> if the world is completed, <code>false</code> otherwise
     */
    @Override
    public boolean canOpen() {
        World world = LevelUp.getInstance().getWorld(mAssociatedWorldId);
        return world != null && world.isCompleted();
    }

    @Override
    public boolean tryOpenInner() {
        if (canOpen()) {
            forceOpen(true);
            return true;
        }

        return false;
    }

    /**
     * Handles world completion events and notifies if the gate can be opened.
     *
     * @param worldCompletedEvent
     */
    @Subscribe
    public void onWorldCompleted(WorldCompletedEvent worldCompletedEvent) {
        if (worldCompletedEvent.World.getWorldId().equals(mAssociatedWorldId)) {
            BusProvider.getInstance().unregister(this);
            // gate can now open
        }
    }


    /** Private Members */

    private static final String TAG = "SOOMLA WorldCompletionGate";

    private String mAssociatedWorldId;
}
