/*
 * Copyright (C) 2012-2015 Soomla Inc.
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

/**
 * Fired when the last completed world inside a world has changed
 */
public class LastCompletedWorldChanged {

    /**
     * World ID which had last completed world changed
     */
    public final String WorldId;

    /**
     * The inner world which was last completed
     */
    public final String InnerWorldId;

    /**
     * Constructor
     * 
     * @param worldId World ID which had last completed world changed
     * @param innerWorldId The inner world which was last completed
     */
    public LastCompletedWorldChanged(String worldId, String innerWorldId) {
        WorldId = worldId;
        InnerWorldId = innerWorldId;
    }
}
