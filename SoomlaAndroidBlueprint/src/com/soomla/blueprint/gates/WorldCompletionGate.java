package com.soomla.blueprint.gates;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.World;
import com.soomla.blueprint.events.GateCanBeOpenedEvent;
import com.soomla.blueprint.events.GateOpenedEvent;
import com.soomla.blueprint.events.WorldCompletedEvent;
import com.soomla.store.BusProvider;
import com.squareup.otto.Subscribe;

/**
 * Created by refaelos on 07/05/14.
 */
public class WorldCompletionGate extends Gate {
    private String mAssociatedWorldId;

    public WorldCompletionGate(String gateId, String associatedWorldId) {
        super(gateId);
        this.mAssociatedWorldId = associatedWorldId;

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    private boolean canPass() {
        World world = Blueprint.getWorld(mAssociatedWorldId);
        return world != null && world.isCompleted();
    }

    @Override
    public void tryOpenInner() {
        if (canPass()) {
            forceOpen(true);
        }
    }

    @Subscribe
    public void onWorldCompleted(WorldCompletedEvent worldCompletedEvent) {
        if (worldCompletedEvent.getWorld().getWorldId().equals(mAssociatedWorldId)) {
            BusProvider.getInstance().unregister(this);
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }
}
