package com.soomla.blueprint.scoring;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Score</code> that has an associated
 * virtual item. The score is related to the specific item ID.  For example:
 * a game that has an "energy" virtual item can have energy points.
 *
 * Created by refaelos on 06/05/14.
 */
public class VirtualItemScore extends Score {

    /**
     * Constructor
     *
     * @param scoreId see parent
     * @param name see parent
     * @param associatedItemId the ID of the virtual item associated with this score
     */
    public VirtualItemScore(String scoreId, String name, String associatedItemId) {
        super(scoreId, name);
        this.mAssociatedItemId = associatedItemId;
    }

    /**
     * Constructor
     *
     * @param scoreId see parent
     * @param name see parent
     * @param higherBetter see parent
     * @param associatedItemId the ID of the virtual item associated with this score
     */
    public VirtualItemScore(String scoreId, String name, boolean higherBetter, String associatedItemId) {
        super(scoreId, name, higherBetter);
        this.mAssociatedItemId = associatedItemId;
    }

    /**
     * Constructor.
     * Generates an instance of <code>VirtualItemScore</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>VirtualItemScore</code>.
     * @throws JSONException
     */
    public VirtualItemScore(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mAssociatedItemId = jsonObject.getString(BPJSONConsts.BP_ASSOCITEMID);
    }

    /**
     * Converts the current <code>VirtualItemScore</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>VirtualItemScore</code>.
     */
    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_ASSOCITEMID, mAssociatedItemId);
            jsonObject.put(BPJSONConsts.BP_TYPE, "item");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }


    /** Setters and Getters */

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA VirtualItemScore";

    private String mAssociatedItemId;
}
