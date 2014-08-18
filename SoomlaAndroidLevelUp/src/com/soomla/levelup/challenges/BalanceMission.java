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

package com.soomla.levelup.challenges;

import com.soomla.levelup.gates.BalanceGate;
import com.soomla.rewards.Reward;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A specific type of <code>Mission</code> that has an associated
 * virtual item and a desired balance which opens a <code>BalanceGate</code>.
 * The mission is completed once the balance gate is opened.
 * <p/>
 * Created by refaelos on 13/05/14.
 */
public class BalanceMission extends Mission {

    /**
     * Constructor
     *
     * @param id               see parent
     * @param name             see parent
     * @param associatedItemId the ID of the item who's balance is examined
     * @param desiredBalance   the balance which will complete this mission
     */
    public BalanceMission(String id, String name, String associatedItemId, int desiredBalance) {
        super(id, name, BalanceGate.class, new Object[]{associatedItemId, desiredBalance});
    }

    /**
     * Constructor
     *
     * @param id               see parent
     * @param name             see parent
     * @param rewards          see parent
     * @param associatedItemId the ID of the item who's balance is examined
     * @param desiredBalance   the balance which will complete this mission
     */
    public BalanceMission(String id, String name, List<Reward> rewards, String associatedItemId, int desiredBalance) {
        super(id, name, rewards, BalanceGate.class, new Object[]{associatedItemId, desiredBalance});
    }

    /**
     * @{inheritDoc}
     */
    public BalanceMission(JSONObject jsonMission) throws JSONException {
        super(jsonMission);
    }

}
