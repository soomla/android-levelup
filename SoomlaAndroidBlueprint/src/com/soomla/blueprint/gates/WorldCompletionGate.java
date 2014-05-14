package com.soomla.blueprint.gates;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.World;
import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.events.GateCanBeOpenedEvent;
import com.soomla.blueprint.events.WorldCompletedEvent;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 07/05/14.
 */
public class WorldCompletionGate extends Gate {
    private static final String TAG = "SOOMLA WorldCompletionGate";
    private String mAssociatedWorldId;

    public WorldCompletionGate(String gateId, String associatedWorldId) {
        super(gateId);
        this.mAssociatedWorldId = associatedWorldId;

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    public WorldCompletionGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedWorldId = jsonObject.getString(BPJSONConsts.BP_ASSOCWORLDID);

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCWORLDID, mAssociatedWorldId);
            jsonObject.put(BPJSONConsts.BP_TYPE, "worldCompletion");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    private boolean canPass() {
        World world = Blueprint.getInstance().getWorld(mAssociatedWorldId);
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
