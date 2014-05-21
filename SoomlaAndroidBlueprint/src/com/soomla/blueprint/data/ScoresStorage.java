package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.scoring.Score;
import com.soomla.store.data.StorageManager;

/**
 * A utility class for persisting and querying scores and records.
 * Use this class to get or set the values of scores and records.
 * This class uses the <code>KeyValueStorage</code> internally for storage.
 *
 * Created by refaelos on 13/05/14.
 */
public class ScoresStorage {

    private static String keyScores(String scoreId, String postfix) {
        return Blueprint.DB_KEY_PREFIX + "scores." + scoreId + "." + postfix;
    }

    private static String keyLatestScore(String scoreId) {
        return keyScores(scoreId, "latest");
    }

    private static String keyRecordScore(String scoreId) {
        return keyScores(scoreId, "record");
    }


    /** Latest Score **/

    /**
     * Saves a new value for the given score
     *
     * @param score the score to change
     * @param latest the latest value to save
     */
    public static void setLatestScore(Score score, double latest) {
        String scoreId = score.getScoreId();
        String key = keyLatestScore(scoreId);
        String val = String.valueOf(latest);

        StorageManager.getKeyValueStorage().setValue(key, val);
    }

    /**
     * Gets the most recently saved value of the given score.
     *
     * @param score the score to examine
     * @return the last saved value
     */
    public static double getLatestScore(Score score) {
        String scoreId = score.getScoreId();
        String key = keyLatestScore(scoreId);
        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val == null ? 0 : Double.parseDouble(val);
    }



    /** Record Score **/

    /**
     * Sets a new record for the given score.
     *
     * @param score the score who's record to change
     * @param record the new record value
     */
    public static void setRecordScore(Score score, double record) {
        String scoreId = score.getScoreId();
        String key = keyRecordScore(scoreId);
        String val = String.valueOf(record);

        StorageManager.getKeyValueStorage().setValue(key, val);
    }

    /**
     * Retrieves the record of the given score
     *
     * @param score the score to examine
     * @return the record of the given score
     */
    public static double getRecordScore(Score score) {
        String scoreId = score.getScoreId();
        String key = keyRecordScore(scoreId);
        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val == null ? 0 : Double.parseDouble(val);
    }

}
