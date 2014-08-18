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

import com.soomla.SoomlaEntity;
import com.soomla.util.JSONFactory;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A gate is an object which defines certain criteria for progressing
 * between the game's <code>World</code>s or <code>Level</code>s.
 * <p/>
 * Created by refaelos on 06/05/14.
 */
public abstract class Gate extends SoomlaEntity<Gate> {

    /**
     * Constructor
     *
     * @param id the gate's ID
     */
    public Gate(String id) {
        this(id, "");
    }

    /**
     * Constructor
     *
     * @param id   the gate's ID
     * @param name the gate's name
     */
    public Gate(String id, String name) {
        super(id, name, "");
    }

    /**
     * Constructor
     * Generates an instance of <code>Gate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>Gate</code>.
     * @throws JSONException
     */
    public Gate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }

    /**
     * Converts the current <code>Gate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>Gate</code>.
     */
    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();
        return jsonObject;
    }

    /**
     * For JNI purposes
     *
     * @param jsonString
     * @return a mission from a JSON string
     */
    public static Gate fromJSONString(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return fromJSONObject(jsonObject);
        } catch (JSONException e) {
            return null;
        }
    }

    public static Gate fromJSONObject(JSONObject jsonObject) {
        return sJSONFactory.create(jsonObject, Gate.class.getPackage().getName());
    }


    /** Setters and Getters */


    /**
     * Private Members
     */

    private static final String TAG = "SOOMLA Gate";

    private static JSONFactory<Gate> sJSONFactory = new JSONFactory<Gate>();
}
