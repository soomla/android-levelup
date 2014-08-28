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

import android.graphics.Bitmap;

import com.soomla.profile.domain.IProvider;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gurdotan on 8/28/14.
 */
public class SocialUploadGate extends SocialActionGate {

    /**
     * Constructor
     *
     * @param id       see parent
     * @param provider The social provider to use to open this gate
     * @param fileName the file to upload
     * @param message  the story's message
     * @param bitmap   the story's bitmap
     */
    public SocialUploadGate(String id, IProvider.Provider provider, String fileName, String message, Bitmap bitmap) {
        super(id, provider);
        mMessage = message;
        mFileName = fileName;
        mBitmap = bitmap;
    }

    /**
     * Constructor
     * Generates an instance of <code>SocialUploadGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws org.json.JSONException
     */
    public SocialUploadGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        // TODO: implement this when needed. It's irrelevant now.
    }

    /**
     * Converts the current <code>SocialUploadGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>SocialUploadGate</code>.
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

    private static String TAG = "SOOMLA SocialUploadGate";

    private String mFileName;
    private String mMessage;
    private Bitmap mBitmap;

}
