package com.soomla.blueprint.scoring;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.store.StoreUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A specific type of <code>Score</code> that has an associated range.
 * The score's value can be only inside the range of values.  For example,
 * a shooting score can on a scale of 10 to 100 according to the user's
 * performance in the game.
 *
 * Created by refaelos on 07/05/14.
 */
public class RangeScore extends Score {

    /**
     * Constructor
     *
     * @param scoreId see parent
     * @param name see parent
     * @param range the range applicable to this score
     */
    public RangeScore(String scoreId, String name, Range range) {
        super(scoreId, name);
        this.mRange = range;
    }

    /**
     * Constructor
     *
     * @param scoreId see parent
     * @param name see parent
     * @param higherBetter see parent
     * @param range the range applicable to this score
     */
    public RangeScore(String scoreId, String name, boolean higherBetter, Range range) {
        super(scoreId, name, higherBetter);
        this.mRange = range;
    }

    /**
     * Constructor.
     * Generates an instance of <code>RangeScore</code> from the given <code>JSONObject</code>.
     *
     * @param jsonObject A JSONObject representation of the wanted <code>RangeScore</code>.
     * @throws JSONException
     */
    public RangeScore(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mRange = new Range(jsonObject.getJSONObject(BPJSONConsts.BP_SCORE_RANGE));
    }

    /**
     * Converts the current <code>RangeScore</code> to a JSONObject.
     *
     * @return A <code>JSONObject</code> representation of the current <code>RangeScore</code>.
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void inc(double amount) {
        if (getTempScore() == mRange.getHigh()) {
            return;
        }
        super.inc(amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dec(double amount) {
        if (getTempScore() == mRange.getLow()) {
            return;
        }
        super.dec(amount);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * A representation of a range, or interval, of values that can
     * be assigned to this score
     */
    public class Range {

        /**
         * Constructor
         *
         * @param low the lowest value possible in the range
         * @param high the highest value possible in the range
         */
        public Range(double low, double high) {
            mLow = low;
            mHigh = high;
        }

        /**
         * Constructor.
         * Generates an instance of <code>Range</code> from the given <code>JSONObject</code>.
         *
         * @param jsonObject A JSONObject representation of the wanted <code>Range</code>.
         * @throws JSONException
         */
        public Range(JSONObject jsonObject) throws JSONException {
            mLow = jsonObject.getDouble(BPJSONConsts.BP_SCORE_RANGE_LOW);
            mHigh = jsonObject.getDouble(BPJSONConsts.BP_SCORE_RANGE_HIGH);
        }

        /**
         * Converts the current <code>Range</code> to a JSONObject.
         *
         * @return A <code>JSONObject</code> representation of the current <code>Range</code>.
         */
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


        /** Setters and Getters */

        public double getLow() {
            return mLow;
        }

        public double getHigh() {
            return mHigh;
        }

        /** Private Members **/

        private static final String TAG = "SOOMLA RangeNumberScore Range";

        private double mLow;
        private double mHigh;
    }


    /** Private Members **/

    private static final String TAG = "SOOMLA RangeScore";

    private Range mRange;
}
