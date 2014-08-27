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

/**
 * This event is fired when <code>Mission</code> completion has been revoked.
 * For example, you can decide to revoke a mission if the condition for completing
 * it is no longer valid.
 */
public class MissionCompletionRevokedEvent {

    /**
     * read-only Properties *
     */

    public final String MissionId;

    /**
     * Constructor
     *
     * @param missionId to be revoked
     */
    public MissionCompletionRevokedEvent(String missionId) {
        MissionId = missionId;
    }
}
