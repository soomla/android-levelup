/*
 * Copyright (C) 2012-2014 Soomla Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soomla.levelup.data;

/**
 * This class contains all static final String names of the keys/vals in the JSON being parsed all
 * around the sdk.
 */
public class LUJSONConsts {

    /** Global **/

    public static final String LU_ASSOCITEMID = "associatedItemId";
    public static final String LU_ASSOCSCOREID = "associatedScoreId";
    public static final String LU_ASSOCWORLDID = "associatedWorldId";
    public static final String LU_DESIRED_RECORD = "desiredRecord";
    public static final String LU_DESIRED_BALANCE = "desiredBalance";
    public static final String LU_NAME = "name";


    /** Score **/

    public static final String LU_SCORES = "scores";
    public static final String LU_SCORE_SCOREID = "scoreId";
    public static final String LU_SCORE_STARTVAL = "startValue";
    public static final String LU_SCORE_HIGHBETTER = "higherBetter";

    public static final String LU_SCORE_RANGE = "range";
    public static final String LU_SCORE_RANGE_LOW = "low";
    public static final String LU_SCORE_RANGE_HIGH = "high";


    /** Reward **/

    public static final String LU_REWARDS = "rewards";
    public static final String LU_REWARD_REWARDID = "rewardId";
    public static final String LU_REWARD_AMOUNT = "amount";
    public static final String LU_REWARD_ICONURL = "iconUrl";
    public static final String LU_REWARD_REPEAT = "repeatable";


    /** Gate **/

    public static final String LU_GATES = "gates";
    public static final String LU_GATE_GATEID = "gateId";


    /** Challenge **/

    public static final String LU_CHALLENGES = "challenges";
    public static final String LU_MISSIONS = "missions";
    public static final String LU_MISSION_MISSIONID = "missionId";


    /** World **/

    public static final String LU_WORLDS = "worlds";
    public static final String LU_WORLD_WORLDID = "worldId";
}

