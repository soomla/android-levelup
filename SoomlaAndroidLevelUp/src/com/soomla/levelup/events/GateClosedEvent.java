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

public class GateClosedEvent {

    /**
     * read-only Properties
     */

    public final String GateId;

    /**
     * Constructor
     *
     * @param gateId the if of the gate that has been opened
     */
    public GateClosedEvent(String gateId) {
        GateId = gateId;
    }
}
