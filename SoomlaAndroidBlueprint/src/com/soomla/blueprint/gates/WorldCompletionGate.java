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

    @Override
    public void open() {
        mOpen = true;
        BusProvider.getInstance().post(new GateOpenedEvent(this));
    }

    @Override
    public boolean isOpen() {
        if (!mOpen) {
            World world = Blueprint.getWorld(mAssociatedWorldId);
            if (world  != null && !world.isCompleted()) {
                return false;
            }

            mOpen = true;
        }

        return true;
    }

    @Subscribe
    public void onWorldCompleted(WorldCompletedEvent worldCompletedEvent) {
        if (worldCompletedEvent.getWorld().getWorldId().equals(mAssociatedWorldId)) {
            BusProvider.getInstance().unregister(this);
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }
}
