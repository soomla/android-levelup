package com.soomla.blueprint.data;

import com.soomla.blueprint.Blueprint;
import com.soomla.blueprint.scoring.Score;
import com.soomla.store.data.StorageManager;

/**
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

    public static void setLatestScore(Score score, double latest) {
        String scoreId = score.getScoreId();
        String key = keyLatestScore(scoreId);
        String val = String.valueOf(latest);

        StorageManager.getKeyValueStorage().setValue(key, val);
    }

    public static double getLatestScore(Score score) {
        String scoreId = score.getScoreId();
        String key = keyLatestScore(scoreId);
        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val == null ? 0 : Double.parseDouble(val);
    }



    /** Record Score **/

    public static void setRecordScore(Score score, double record) {
        String scoreId = score.getScoreId();
        String key = keyRecordScore(scoreId);
        String val = String.valueOf(record);

        StorageManager.getKeyValueStorage().setValue(key, val);
    }

    public static double getRecordScore(Score score) {
        String scoreId = score.getScoreId();
        String key = keyRecordScore(scoreId);
        String val = StorageManager.getKeyValueStorage().getValue(key);

        return val == null ? 0 : Double.parseDouble(val);
    }

}
