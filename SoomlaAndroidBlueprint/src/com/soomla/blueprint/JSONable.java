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

package com.soomla.blueprint;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An interface that enforces implementers to be able to marshall
 * instances of themselves to a <code>JSONObject</code>.
 *
 * Created by gurdotan on 5/21/14.
 */
public interface JSONable {

    /**
     * Converts the instance of this object to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current object.
     */
    public JSONObject toJSONObject() throws JSONException;

}
