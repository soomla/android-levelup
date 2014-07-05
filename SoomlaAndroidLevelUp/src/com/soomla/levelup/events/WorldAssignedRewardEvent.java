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

package com.soomla.levelup.events;

import com.soomla.levelup.World;

/**
 * This event is fired when a <code>World</code> is being assigned a reward.
 */
public class WorldAssignedRewardEvent {

    /** read-only Properties **/

    public final World World;

    /**
     * Constructor
     *
     * @param world the World whose reward has changed
     */
    public WorldAssignedRewardEvent(World world) {
        World = world;
    }
}
