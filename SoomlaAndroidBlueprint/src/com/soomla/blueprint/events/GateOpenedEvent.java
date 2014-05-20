/*
 * Copyright (C) 2012 Soomla Inc.
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
package com.soomla.blueprint.events;

import com.soomla.blueprint.gates.Gate;

/**
 * This event is fired when a <code>Gate</code> has been opened.
 */
public class GateOpenedEvent {

    /**
     * Constructor
     *
     * @param gate the gate that has been opened
     */
    public GateOpenedEvent(Gate gate) {
        mGate = gate;
    }

    /** Setters and Getters */

    public Gate getGate() {
        return mGate;
    }

    /** Private Members */

    private Gate mGate;
}
