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
import com.soomla.util.JSONFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A representation of one or more <code>Gate</code>s which together define
 * a composite criteria for progressing between the game's
 * <code>World</code>s or <code>Level</code>s.
 *
 * Created by refaelos on 06/05/14.
 */
public abstract class GatesList extends Gate {

    /**
     * Constructor
     *
     * @param gateId see parent
     */
    public GatesList(String gateId) {
        super(gateId);
        mGates = new ArrayList<Gate>();

        // "fake" gates with 1 sub-gate are auto open
        mAutoOpenBehavior = true;

        registerEvents();
    }

    /**
     * Constructor
     * Initializes with only one <code>Gate</code> instance
     *
     * @param gateId see parent
     * @param singleGate the only gate to add to the list
     */
    public GatesList(String gateId, Gate singleGate) {
        super(gateId);
        mGates = new ArrayList<Gate>();
        mGates.add(singleGate);

        // "fake" gates with 1 sub-gate are auto open
        mAutoOpenBehavior = true;

        registerEvents();
    }

    /**
     * Constructor
     * Initializes with a given list of <code>Gate</code>s
     *
     * @param gateId see parent
     * @param gates a list of <code>Gate</code>s to use
     */
    public GatesList(String gateId, List<Gate> gates) {
        super(gateId);
        mGates = gates;

        registerEvents();
    }

    /**
     * Constructor
     * Generates an instance of <code>GatesList</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject
     * @throws JSONException
     */
    public GatesList(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mGates = new ArrayList<Gate>();
        JSONArray gatesArr = jsonObject.getJSONArray(BPJSONConsts.BP_GATES);

        // Iterate over all gates in the JSON array and for each one create
        // an instance according to the gate type
        for (int i = 0; i < gatesArr.length(); i++) {
            JSONObject gateJSON = gatesArr.getJSONObject(i);
            Gate gate = Gate.fromJSONObject(gateJSON);
            if (gate != null) {
                mGates.add(gate);
            }
        }

        if (mGates.size() < 2) {
            // "fake" gates with 1 sub-gate are auto open
            mAutoOpenBehavior = true;
        }

        registerEvents();
    }

    private void registerEvents() {
        if(!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    protected void unregisterEvents() {
        BusProvider.getInstance().unregister(this);
    }

    /**
     * Converts the current <code>GatesList</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>GatesList</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            JSONArray gatesArr = new JSONArray();
            for (Gate gate : mGates) {
                gatesArr.put(gate.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_GATES, gatesArr);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public static GatesList fromJSONObject(JSONObject jsonObject) {
        return sJSONFactory.create(jsonObject, sTypeMap);
    }

    public void addGate(Gate gate) {
        mGates.add(gate);
    }

    public int size() {
        return mGates.size();
    }

    /**
     * Attempts to open all gates included in this gate list
     */
    @Override
    public boolean tryOpenInner() {
        if(mAutoOpenBehavior) {
            for (Gate gate : mGates) {
                gate.tryOpen();
            }

            return isOpen();
        }
        else {
            if (canOpen()) {
                forceOpen(true);
                return true;
            }

            return false;
        }
    }

    /** Setters and Getters */

    public List<Gate> getGates() {
        return mGates;
    }

    /** Private Members */

    private static final String TAG = "SOOMLA GatesList";

    private static JSONFactory<GatesList> sJSONFactory = new JSONFactory<GatesList>();
    private static Map<String, Class<? extends GatesList>> sTypeMap =
            new HashMap<String, Class<? extends GatesList>>(2);
    static {
        sTypeMap.put(GatesListAND.TYPE_NAME, GatesListAND.class);
        sTypeMap.put(GatesListOR.TYPE_NAME, GatesListOR.class);
    }

    protected List<Gate> mGates;

    // does opening child gates cause us to auto-open (or just canOpen)?
    protected boolean mAutoOpenBehavior = false;
}
