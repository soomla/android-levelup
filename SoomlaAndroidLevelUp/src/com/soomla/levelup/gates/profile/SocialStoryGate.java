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

import com.soomla.profile.domain.IProvider;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gurdotan on 8/28/14.
 */
public class SocialStoryGate extends SocialActionGate {

    /**
     * Constructor
     *
     * @param id       see parent
     * @param provider The social provider to use to open this gate
     * @param message  the story's message
     * @param name     the story's name
     * @param caption  the story's caption
     * @param link     the story's link
     * @param url      the story's URL
     */
    public SocialStoryGate(String id, IProvider.Provider provider, String message, String name, String caption, String link, String url) {
        super(id, provider);
        mMessage = message;
        mName = name;
        mCaption = caption;
        mLink = link;
        mUrl = url;
    }

    /**
     * Constructor
     * Generates an instance of <code>SocialStoryGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws org.json.JSONException
     */
    public SocialStoryGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        // TODO: implement this when needed. It's irrelevant now.
    }

    /**
     * Converts the current <code>SocialStoryGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>SocialStoryGate</code>.
     */
    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();

        // TODO: implement this when needed. It's irrelevant now.

        return jsonObject;
    }


    /**
     * Private Members
     */

    private static String TAG = "SOOMLA SocialStoryGate";

    private String mMessage;
    private String mName;
    private String mCaption;
    private String mLink;
    private String mUrl;

}
