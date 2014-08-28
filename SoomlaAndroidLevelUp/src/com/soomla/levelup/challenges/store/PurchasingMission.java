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

package com.soomla.levelup.challenges.store;

import com.soomla.levelup.challenges.Mission;
import com.soomla.levelup.gates.store.PurchasableGate;
import com.soomla.rewards.Reward;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A specific type of <code>Mission</code> which has no specific implementation
 * and allows the developer to define custom criteria for mission completion.
 * Override the {@link com.soomla.levelup.challenges.Mission#isCompleted()} in order to the define the custom criteria.
 * <p/>
 * Created by refaelos on 13/05/14.
 */
public class PurchasingMission extends Mission {

    /**
     * Constructor
     *
     * @param id               see parent
     * @param name             see parent
     * @param associatedItemId the ID of the item who's balance is examined
     */
    public PurchasingMission(String id, String name, String associatedItemId) {
        super(id, name, PurchasableGate.class, new Object[]{associatedItemId});
    }

    /**
     * Constructor
     *
     * @param id               see parent
     * @param name             see parent
     * @param rewards          see parent
     * @param associatedItemId the ID of the item who's balance is examined
     */
    public PurchasingMission(String id, String name, List<Reward> rewards, String associatedItemId) {
        super(id, name, rewards, PurchasableGate.class, new Object[]{associatedItemId});
    }

    /**
     * @{inheritDoc}
     */
    public PurchasingMission(JSONObject jsonMission) throws JSONException {
        super(jsonMission);
    }


}
