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

package com.soomla.blueprint.gates;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.data.GatesStorage;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A gate is an object which defines certain criteria for progressing
 * between the game's <code>World</code>s or <code>Level</code>s.
 *
 * Created by refaelos on 06/05/14.
 */
public abstract class Gate {

    /**
     * Constructor
     *
     * @param gateId the gate's ID
     */
    public Gate(String gateId) {
        this.mGateId = gateId;
    }

    /**
     * Constructor
     * Generates an instance of <code>Gate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Gate</code>.
     * @throws JSONException
     */
    public Gate(JSONObject jsonObject) throws JSONException {
        mGateId = jsonObject.getString(BPJSONConsts.BP_GATE_GATEID);
    }

    /**
     * Converts the current <code>Gate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Gate</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_GATE_GATEID, mGateId);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * Attempts to open this gate
     */
    public void tryOpen() {
        if (GatesStorage.isOpen(this)) {
            return;
        }

        tryOpenInner();
    }

    protected abstract void tryOpenInner();

    /**
     * Sets the gate to be opened
     * @param open
     */
    public void forceOpen(boolean open) {
        GatesStorage.setOpen(this, open);
    }

    /**
     * Checks whether this gate is open
     *
     * @return <code>true</code> if open,<code>false</code> otherwise
     */
    public boolean isOpen() {
        return GatesStorage.isOpen(this);
    }


    /** Setters and Getters */

    public String getGateId() {
        return mGateId;
    }


    /** Private Members */

    private static final String TAG = "SOOMLA Gate";

    private String mGateId;
}
