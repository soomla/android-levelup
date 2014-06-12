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

import com.soomla.levelup.LevelUp;
import com.soomla.levelup.World;
import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.events.GateCanBeOpenedEvent;
import com.soomla.levelup.events.WorldCompletedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;
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
            jsonObject.put(BPJSONConsts.BP_TYPE, "worldCompletion");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Checks if the gate meets its world completion criteria for opening.
     *
     * @return <code>true</code> if the world is completed, <code>false</code> otherwise
     */
    private boolean canPass() {
        World world = LevelUp.getInstance().getWorld(mAssociatedWorldId);
        return world != null && world.isCompleted();
    }

    @Override
    public void tryOpenInner() {
        if (canPass()) {
            forceOpen(true);
        }
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
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }


    /** Private Members */

    private static final String TAG = "SOOMLA WorldCompletionGate";

    private String mAssociatedWorldId;
}
