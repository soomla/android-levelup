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

import com.soomla.Schedule;
import com.soomla.SoomlaUtils;
import com.soomla.data.JSONConsts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Gate</code> that has an associated schedule.
 * The gate opens when the schedule approves this gate.
 * <p/>
 * Created by gurdotan on 8/18/14.
 */
public class ScheduleGate extends Gate {


    /**
     * Constructor
     *
     * @param id       see parent
     * @param schedule the schedule which will open this gate
     */
    public ScheduleGate(String id, Schedule schedule) {
        super(id);
        mSchedule = schedule;
    }

    /**
     * Constructor
     * Generates an instance of <code>ScheduleGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public ScheduleGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mSchedule = new Schedule(jsonObject.getJSONObject(JSONConsts.SOOM_SCHEDULE));
    }


    /**
     * Converts the current <code>ScheduleGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>ScheduleGate</code>.
     */
    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(JSONConsts.SOOM_SCHEDULE, mSchedule.toJSONObject());
        } catch (JSONException e) {
            SoomlaUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }


    /**
     * Setters and Getters
     */

    public Schedule getSchedule() {
        return mSchedule;
    }


    /**
     * Private Members
     */

    private static String TAG = "SOOMLA ScheduleGate";

    private Schedule mSchedule;

}
