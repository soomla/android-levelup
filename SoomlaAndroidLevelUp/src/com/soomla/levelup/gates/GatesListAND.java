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
import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.levelup.data.GateStorage;
import com.soomla.levelup.events.GateCanBeOpenedEvent;
import com.soomla.levelup.events.GateOpenedEvent;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A specific type of <code>GatesList</code> which can be opened
 * only if <b>ALL</b> gates in its list are open.
 *
 * Inheritance: GatesListAND >
 * {@link com.soomla.levelup.gates.GatesList} >
 * {@link com.soomla.levelup.gates.Gate}
 *
 * Created by refaelos on 07/05/14.
 */
public class GatesListAND extends GatesList {

    public static final String TYPE_NAME = "listAND";

    /**
     * Constructor
     *
     * @param gateId see parent
     */
    public GatesListAND(String gateId) {
        super(gateId);
    }

    /**
     * Constructor
     *
     * @param gateId see parent
     * @param singleGate see parent
     */
    public GatesListAND(String gateId, Gate singleGate) {
        super(gateId, singleGate);
    }

    /**
     * Constructor
     *
     * @param gateId see parent
     * @param gates see parent
     */
    public GatesListAND(String gateId, List<Gate> gates) {
        super(gateId, gates);
    }

    /**
     * Constructor
     * Generates an instance of <code>GatesListAND</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public GatesListAND(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    /**
     * Converts the current <code>GatesListAND</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>GatesListAND</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_TYPE, TYPE_NAME);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
        if(mAutoOpenBehavior) {
            for (Gate gate : mGates) {
                if (!gate.isOpen()) {
                    return false;
                }
            }
            return true;
        }
        else {
            return super.isOpen();
        }
    }

    @Override
    public boolean canOpen() {
        for (Gate gate : mGates) {
            if ((mChildrenCanOpenIsEnough && !gate.canOpen()) || !gate.isOpen()) {
                return false;
            }
        }
        return true;
    }

    /** Events **/

    // If canOpen is defined as true when all sub-gates canOpen
    // use this subscription

    @Subscribe
    public void onGateCanOpenEvent(GateCanBeOpenedEvent gateCanBeOpenedEvent) {
        if(!mChildrenCanOpenIsEnough || !mGates.contains(gateCanBeOpenedEvent.Gate))
            return; // handled by GateOpenedEvent

        boolean allCanOpen = true;
        for (Gate gate : mGates) {
            if (!gate.canOpen()) {
                allCanOpen = false;
                break;
            }
        }

        if (allCanOpen) {
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }

    // If canOpen is defined as true when all sub-gates isOpen
    // use this subscription

    @Subscribe
    public void onGateOpenedEvent(GateOpenedEvent gateOpenedEvent) {
        if(mChildrenCanOpenIsEnough || !mGates.contains(gateOpenedEvent.Gate))
            return; // handled by GateCanBeOpenedEvent

        boolean allOpen = true;
        for (Gate gate : mGates) {
            if (/*!gate.isOpen()*/!GateStorage.isOpen(gate)) {
                allOpen = false;
                break;
            }
        }

        if (allOpen && !GateStorage.isOpen(this)) {
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }



    /** Private Members */

    private static final String TAG = "SOOMLA GatesListAND";
}
