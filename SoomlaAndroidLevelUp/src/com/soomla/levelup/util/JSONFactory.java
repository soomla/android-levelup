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

package com.soomla.levelup.util;

import com.soomla.levelup.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Utility to create LevelUp objects from JSON
 * (Pretty much one step before using Gson/Jackson)
 */
public class JSONFactory<T> {
    public static final String TAG = "JSONFactory";

    public T create(JSONObject jsonObject, Map<String, Class<? extends T>> typeMap) {
        if (jsonObject == null) {
            // warn
            return null;
        }

        T t = null;
        try {
            String type = jsonObject.getString(BPJSONConsts.BP_TYPE);
            Class<? extends T> clazz = typeMap.get(type);
            if (clazz != null) {
                final Constructor<? extends T> jsonCtor = clazz.getDeclaredConstructor(JSONObject.class);
                t = jsonCtor.newInstance(jsonObject);
            }
            else {
                StoreUtils.LogError(TAG, "unknown type:" + type);
            }

        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "fromJSONObject JSONException:" + e.getMessage());
        } catch (InstantiationException e) {
            StoreUtils.LogError(TAG, "fromJSONObject InstantiationException:" + e.getMessage());
        } catch (IllegalAccessException e) {
            StoreUtils.LogError(TAG, "fromJSONObject IllegalAccessException:" + e.getMessage());
        } catch (NoSuchMethodException e) {
            StoreUtils.LogError(TAG, "fromJSONObject no JSONObject constructor found:" + e.getMessage());
        } catch (InvocationTargetException e) {
            StoreUtils.LogError(TAG, "fromJSONObject InvocationTargetException:" + e.getMessage());
        }

        return t;
    }
}
