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

package com.soomla.levelup.challenges.profile;

import com.soomla.levelup.challenges.Mission;
import com.soomla.levelup.gates.profile.SocialStoryGate;
import com.soomla.profile.domain.IProvider;
import com.soomla.rewards.Reward;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by gurdotan on 8/28/14.
 */
public class SocialStoryMission extends Mission {

    //string message, string storyName, string caption, string link, string imgUrl

    /**
     * Constructor
     *
     * @param id        see parent
     * @param name      see parent
     * @param provider  the social provider to use
     * @param message   the story's message
     * @param storyName the story's name
     * @param caption   the story's caption
     * @param link      the story's link
     * @param imgUrl    ths story's image URL
     */
    public SocialStoryMission(String id, String name, IProvider.Provider provider, String message, String storyName, String caption, String link, String imgUrl) {
        super(id, name, SocialStoryGate.class, new Object[]{provider, message, storyName, caption, link, imgUrl});
    }

    /**
     * Constructor
     *
     * @param id        see parent
     * @param name      see parent
     * @param rewards   see parent
     * @param provider  the social provider to use
     * @param message   the story's message
     * @param storyName the story's name
     * @param caption   the story's caption
     * @param link      the story's link
     * @param imgUrl    ths story's image URL
     */
    public SocialStoryMission(String id, String name, List<Reward> rewards, IProvider.Provider provider, String message, String storyName, String caption, String link, String imgUrl) {
        super(id, name, rewards, SocialStoryGate.class, new Object[]{provider, message, storyName, caption, link, imgUrl});
    }

    /**
     * @{inheritDoc}
     */
    public SocialStoryMission(JSONObject jsonMission) throws JSONException {
        super(jsonMission);

        // TODO: implement this when needed. It's irrelevant now.
    }

    /**
     * Converts the current <code>SocialStoryMission</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>SocialStoryMission</code>.
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

    private static String TAG = "SOOMLA SocialStoryMission";

}
