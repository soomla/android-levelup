package com.soomla.blueprint.scoring;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by refaelos on 07/05/14.
 */
public class RangeScore extends Score {
    private static final String TAG = "SOOMLA RangeScore";
    private Range mRange;

    public RangeScore(String scoreId, String name, Range range) {
        super(scoreId, name);
        this.mRange = range;
    }

    public RangeScore(String scoreId, String name, boolean higherBetter, Range range) {
        super(scoreId, name, higherBetter);
        this.mRange = range;
    }

    public RangeScore(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mRange = new Range(jsonObject.getJSONObject(BPJSONConsts.BP_SCORE_RANGE));
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            jsonObject.put(BPJSONConsts.BP_SCORE_RANGE, mRange.toJSONObject());
            jsonObject.put(BPJSONConsts.BP_TYPE, "range");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    @Override
    public void inc(double amount) {
        if (getTempScore() == mRange.getHigh()) {
            return;
        }
        super.inc(amount);
    }

    @Override
    public void dec(double amount) {
        if (getTempScore() == mRange.getLow()) {
            return;
        }
        super.dec(amount);
    }

    @Override
    public void setTempScore(double score) {
        if (score > mRange.getHigh()) {
            score = mRange.getHigh();
        }
        if (score < mRange.getLow()) {
            score = mRange.getLow();
        }
        super.setTempScore(score);
    }

    public class Range {
        private static final String TAG = "SOOMLA RangeNumberScore Range";
        private double mLow;
        private double mHigh;

        public Range(double low, double high) {
            mLow = low;
            mHigh = high;
        }

        public Range(JSONObject jsonObject) throws JSONException {
            mLow = jsonObject.getDouble(BPJSONConsts.BP_SCORE_RANGE_LOW);
            mHigh = jsonObject.getDouble(BPJSONConsts.BP_SCORE_RANGE_HIGH);
        }

        public JSONObject toJSONObject(){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(BPJSONConsts.BP_SCORE_RANGE_LOW, mLow);
                jsonObject.put(BPJSONConsts.BP_SCORE_RANGE_HIGH, mHigh);
            } catch (JSONException e) {
                StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
            }

            return jsonObject;
        }

        public double getLow() {
            return mLow;
        }

        public double getHigh() {
            return mHigh;
        }
    }
}
