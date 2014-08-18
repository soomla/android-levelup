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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A specific type of <code>GatesList</code> which can be opened
 * if <b>AT LEAST ONE</b> gate in its list is open.
 * <p/>
 * Inheritance: GatesListOR >
 * {@link com.soomla.levelup.gates.GatesList} >
 * {@link com.soomla.levelup.gates.Gate}
 * <p/>
 * Created by refaelos on 07/05/14.
 */
public class GatesListOR extends GatesList {


    /**
     * Constructor
     *
     * @param id see parent
     */
    public GatesListOR(String id) {
        super(id);
    }

    /**
     * Constructor
     *
     * @param id         see parent
     * @param singleGate see parent
     */
    public GatesListOR(String id, Gate singleGate) {
        super(id, singleGate);
    }

    /**
     * Constructor
     *
     * @param id    see parent
     * @param gates see parent
     */
    public GatesListOR(String id, List<Gate> gates) {
        super(id, gates);
    }

    /**
     * Constructor
     * Generates an instance of <code>GatesListOR</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public GatesListOR(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }


    /**
     * Private Members
     */

    private static final String TAG = "SOOMLA GatesListOR";
}
