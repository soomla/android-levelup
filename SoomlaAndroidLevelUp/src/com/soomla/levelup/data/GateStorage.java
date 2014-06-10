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

package com.soomla.levelup.data;

import com.soomla.levelup.LevelUp;
import com.soomla.levelup.events.GateOpenedEvent;
import com.soomla.levelup.gates.Gate;
import com.soomla.store.BusProvider;
import com.soomla.store.data.StorageManager;

/**
 * A utility class for persisting and querying the state of gates.
 * Use this class to check if a certain gate is open, or to open it.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 *
 * Created by refaelos on 13/05/14.
 */
public class GateStorage {

    private static String keyGates(String gateId, String postfix) {
        return LevelUp.DB_KEY_PREFIX + "gates." + gateId + "." + postfix;
    }

    private static String keyGateOpen(String gateId) {
        return keyGates(gateId, "open");
    }

    /**
     * Opens or closes the given gate.
     *
     * @param gate the gate to change status
     * @param open the status (<code>true</code> for open,
     *             <code>false</code> for closed)
     */
    public static void setOpen(Gate gate, boolean open) {
        setOpen(gate, open, true);
    }

    public static void setOpen(Gate gate, boolean open, boolean notify) {
        String gateId = gate.getGateId();
        String key = keyGateOpen(gateId);

        if (open) {
            StorageManager.getKeyValueStorage().setValue(key, "yes");

            if (notify) {
                BusProvider.getInstance().post(new GateOpenedEvent(gate));
            }
        } else {
            StorageManager.getKeyValueStorage().deleteKeyValue(key);
        }
    }

    /**
     * Checks if the given gate is open.
     *
     * @param gate the gate to check
     * @return <code>true</code> if open, <code>false</code> otherwise
     */
    public static boolean isOpen(Gate gate) {
        String gateId = gate.getGateId();
        String key = keyGateOpen(gateId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val != null;
    }
}
