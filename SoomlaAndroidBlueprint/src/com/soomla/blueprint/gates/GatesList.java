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
import com.soomla.store.StoreUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
            String type = gateJSON.getString(BPJSONConsts.BP_TYPE);
            if (type.equals("balance")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("listAND")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("listOR")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("record")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("purchasable")) {
                mGates.add(new BalanceGate(gateJSON));
            } else if (type.equals("worldCompletion")) {
                mGates.add(new BalanceGate(gateJSON));
            } else {
                StoreUtils.LogError(TAG, "Unknown gate type: " + type);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            JSONArray gatesArr = new JSONArray();
            for (Gate gate : mGates) {
                gatesArr.put(gate.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_GATES, gatesArr);
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    public void addGate(Gate gate) {
        mGates.add(gate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean isOpen();

    public int size() {
        return mGates.size();
    }

    /**
     * Attempts to open all gates included in this gate list
     */
    @Override
    public void tryOpenInner() {
        for (Gate gate : mGates) {
            gate.tryOpen();
        }
    }


    /** Setters and Getters */

    public List<Gate> getGates() {
        return mGates;
    }


    /** Private Members */

    private static final String TAG = "SOOMLA GatesList";

    protected List<Gate> mGates;
}
