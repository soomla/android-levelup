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

package com.soomla.levelup.scoring;

import com.soomla.SoomlaUtils;
import com.soomla.levelup.data.LUJSONConsts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>ScoreId</code> that has an associated
 * virtual item. The score is related to the specific item ID.  For example:
 * a game that has an "energy" virtual item can have energy points.
 * <p/>
 * Created by refaelos on 06/05/14.
 */
public class VirtualItemScore extends Score {


    /**
     * Constructor
     *
     * @param id               see parent
     * @param associatedItemId the ID of the virtual item associated with this score
     */
    public VirtualItemScore(String id, String associatedItemId) {
        super(id);
        this.mAssociatedItemId = associatedItemId;
    }

    /**
     * Constructor
     *
     * @param id               see parent
     * @param name             see parent
     * @param higherBetter     see parent
     * @param associatedItemId the ID of the virtual item associated with this score
     */
    public VirtualItemScore(String id, String name, boolean higherBetter, String associatedItemId) {
        super(id, name, higherBetter);
        this.mAssociatedItemId = associatedItemId;
    }

    /**
     * Constructor.
     * Generates an instance of <code>VirtualItemScore</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>VirtualItemScore</code>.
     * @throws JSONException
     */
    public VirtualItemScore(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(LUJSONConsts.LU_ASSOCITEMID);
    }

    /**
     * Converts the current <code>VirtualItemScore</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>VirtualItemScore</code>.
     */
    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(LUJSONConsts.LU_ASSOCITEMID, mAssociatedItemId);
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }


    /**
     * Setters and Getters
     */

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }

    /**
     * Private Members *
     */

    private static final String TAG = "SOOMLA VirtualItemScore";

    private String mAssociatedItemId;
}
