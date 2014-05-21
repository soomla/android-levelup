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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A specific type of <code>GatesList</code> which can be opened
 * only if <b>ALL</b> gates in its list are open.
 *
 * Inheritance: GatesListAND >
 * {@link com.soomla.blueprint.gates.GatesList} >
 * {@link com.soomla.blueprint.gates.Gate}
 *
 * Created by refaelos on 07/05/14.
 */
public class GatesListAND extends GatesList {
    
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
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_TYPE, "listAND");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpen() {
        for (Gate gate : mGates) {
            if (!gate.isOpen()) {
                return false;
            }
        }
        return true;
    }


    /** Private Members */

    private static final String TAG = "SOOMLA GatesListAND";
}
