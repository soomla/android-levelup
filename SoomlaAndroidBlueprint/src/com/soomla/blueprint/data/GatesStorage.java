package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.World;
import com.soomla.blueprint.events.GateOpenedEvent;
import com.soomla.blueprint.events.WorldCompletedEvent;
import com.soomla.blueprint.gates.Gate;
import com.soomla.store.BusProvider;
import com.soomla.store.data.StorageManager;

/**
 * Created by refaelos on 13/05/14.
 */
public class GatesStorage {

    private static String keyGates(String gateId, String postfix) {
        return Blueprint.DB_KEY_PREFIX + "gates." + gateId + "." + postfix;
    }

    private static String keyGateOpen(String gateId) {
        return keyGates(gateId, "open");
    }

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

    public static boolean isOpen(Gate gate) {
        String gateId = gate.getGateId();
        String key = keyGateOpen(gateId);

        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val != null;
    }
}
