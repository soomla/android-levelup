package com.soomla.blueprint.scoring;

/**
 * Created by refaelos on 06/05/14.
 */
public class VirtualItemScore extends Score {
    private String mAssociatedItemId;

    public VirtualItemScore(String scoreId, String name, String associatedItemId) {
        super(scoreId, name);
        this.mAssociatedItemId = associatedItemId;
    }

    public VirtualItemScore(String scoreId, String name, boolean higherBetter, String associatedItemId) {
        super(scoreId, name, higherBetter);
        this.mAssociatedItemId = associatedItemId;
    }

    public String getAssociatedItemId() {
        return mAssociatedItemId;
    }
}
