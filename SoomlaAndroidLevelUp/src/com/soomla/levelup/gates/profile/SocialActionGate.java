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

package com.soomla.levelup.gates.profile;

import com.soomla.SoomlaUtils;
import com.soomla.levelup.data.LUJSONConsts;
import com.soomla.levelup.gates.Gate;
import com.soomla.profile.domain.IProvider;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gurdotan on 8/28/14.
 */
public class SocialActionGate extends Gate {


    /**
     * Constructor
     *
     * @param id       see parent
     * @param provider The social provider to use to open this gate
     */
    public SocialActionGate(String id, IProvider.Provider provider) {
        super(id);
        mProvider = provider;
    }

    /**
     * Constructor
     * Generates an instance of <code>SocialActionGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public SocialActionGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mProvider = IProvider.Provider.getEnum(jsonObject.getString(LUJSONConsts.LU_SOCIAL_PROVIDER));
    }

    /**
     * Converts the current <code>SocialActionGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>SocialActionGate</code>.
     */
    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(LUJSONConsts.LU_SOCIAL_PROVIDER, mProvider.getValue());
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }


    /**
     * Private Members
     */

    private static String TAG = "SOOMLA SocialActionGate";

    private IProvider.Provider mProvider;
}
