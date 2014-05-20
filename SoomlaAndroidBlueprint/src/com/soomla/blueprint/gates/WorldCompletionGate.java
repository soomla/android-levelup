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
 * A specific type of <code>Gate</code> that has an associated
 * world. The gate opens once the world has been completed.
 *
 * Created by refaelos on 07/05/14.
 */
public class WorldCompletionGate extends Gate {

    /**
     * Constructor
     *
     * @param gateId see parent
     * @param associatedWorldId the ID of the world which needs to be completed
     */
    public WorldCompletionGate(String gateId, String associatedWorldId) {
        super(gateId);
        this.mAssociatedWorldId = associatedWorldId;

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Constructor
     * Generates an instance of <code>WorldCompletionGate</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject see parent
     * @throws JSONException
     */
    public WorldCompletionGate(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedWorldId = jsonObject.getString(BPJSONConsts.BP_ASSOCWORLDID);

        if (!isOpen()) {
            BusProvider.getInstance().register(this);
        }
    }

    /**
     * Converts the current <code>WorldCompletionGate</code> to a <code>JSONObject</code>.
     *
     * @return A <code>JSONObject</code> representation of the current <code>WorldCompletionGate</code>.
     */
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

    /**
     * Checks if the gate meets its world completion criteria for opening.
     *
     * @return <code>true</code> if the world is completed, <code>false</code> otherwise
     */
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

    /**
     * Handles world completion events and notifies if the gate can be opened.
     *
     * @param worldCompletedEvent
     */
    @Subscribe
    public void onWorldCompleted(WorldCompletedEvent worldCompletedEvent) {
        if (worldCompletedEvent.getWorld().getWorldId().equals(mAssociatedWorldId)) {
            BusProvider.getInstance().unregister(this);
            BusProvider.getInstance().post(new GateCanBeOpenedEvent(this));
        }
    }


    /** Private Members */

    private static final String TAG = "SOOMLA WorldCompletionGate";

    private String mAssociatedWorldId;
}
