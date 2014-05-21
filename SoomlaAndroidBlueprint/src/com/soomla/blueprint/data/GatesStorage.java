package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.events.GateOpenedEvent;
import com.soomla.blueprint.gates.Gate;
import com.soomla.store.BusProvider;
import com.soomla.store.data.StorageManager;

/**
 * A utility class for persisting and querying the state of gates.
 * Use this class to check if a certain gate is open, or to open it.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 *
 * Created by refaelos on 13/05/14.
 */
public class GatesStorage {

    private static String keyGates(String gateId, String postfix) {
        return Blueprint.DB_KEY_PREFIX + "gates." + gateId + "." + postfix;
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
