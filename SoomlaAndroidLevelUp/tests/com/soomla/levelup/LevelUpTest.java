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

package com.soomla.levelup;

import android.annotation.TargetApi;
import android.os.Build;

import com.soomla.BusProvider;
import com.soomla.Soomla;
import com.soomla.SoomlaApp;
import com.soomla.events.RewardGivenEvent;
import com.soomla.events.RewardTakenEvent;
import com.soomla.levelup.challenges.ActionMission;
import com.soomla.levelup.challenges.BalanceMission;
import com.soomla.levelup.challenges.Challenge;
import com.soomla.levelup.challenges.Mission;
import com.soomla.levelup.challenges.RecordMission;
import com.soomla.levelup.events.GateCanBeOpenedEvent;
import com.soomla.levelup.events.GateOpenedEvent;
import com.soomla.levelup.events.LevelEndedEvent;
import com.soomla.levelup.events.LevelStartedEvent;
import com.soomla.levelup.events.MissionCompletedEvent;
import com.soomla.levelup.events.MissionCompletionRevokedEvent;
import com.soomla.levelup.events.ScoreRecordChangedEvent;
import com.soomla.levelup.events.WorldCompletedEvent;
import com.soomla.levelup.gates.BalanceGate;
import com.soomla.levelup.gates.Gate;
import com.soomla.levelup.gates.GatesListAND;
import com.soomla.levelup.gates.GatesListOR;
import com.soomla.levelup.gates.PurchasableGate;
import com.soomla.levelup.gates.RecordGate;
import com.soomla.levelup.gates.WorldCompletionGate;
import com.soomla.levelup.scoring.RangeScore;
import com.soomla.levelup.scoring.Score;
import com.soomla.levelup.scoring.VirtualItemScore;
import com.soomla.rewards.BadgeReward;
import com.soomla.rewards.Reward;
import com.soomla.store.IStoreAssets;
import com.soomla.store.SoomlaStore;
import com.soomla.store.StoreInventory;
import com.soomla.store.domain.NonConsumableItem;
import com.soomla.store.domain.VirtualCategory;
import com.soomla.store.domain.rewards.VirtualItemReward;
import com.soomla.store.domain.virtualCurrencies.VirtualCurrency;
import com.soomla.store.domain.virtualCurrencies.VirtualCurrencyPack;
import com.soomla.store.domain.virtualGoods.SingleUseVG;
import com.soomla.store.domain.virtualGoods.VirtualGood;
import com.soomla.store.events.GoodBalanceChangedEvent;
import com.soomla.store.exceptions.InsufficientFundsException;
import com.soomla.store.exceptions.VirtualItemNotFoundException;
import com.soomla.store.purchaseTypes.PurchaseWithMarket;
import com.squareup.otto.Subscribe;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by oriargov on 6/12/14.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@Config(emulateSdk = 18,
        manifest = "tests/AndroidManifest.xml"
        ,shadows = { ShadowSQLiteOpenHelper.class }
)
@RunWith(RobolectricTestRunner.class)
public class LevelUpTest {

    public static final String ITEM_ID_BALANCE_GATE = "item_balance_gate";
    public static final String ITEM_ID_BALANCE_MISSION = "balance_mission_item_id";
    public static final String ITEM_ID_BALANCE_MISSION_REWARD = "balance_mission_reward_item_id";
    public static final String ITEM_ID_PURCHASE_GATE_VI = "item_purchase_gate_vi";
    public static final String ITEM_ID_PURCHASE_GATE_MARKET = "item_purchase_gate_market";
    public static final String ITEM_ID_VI_SCORE = "item_vi_score";
    public static final String ITEM_ID_VI_REWARD = "item_vi_reward";

    private static boolean firstSetupDone = false;

    /** event expectations **/

    private String mExpectedMissionEventId = "";
    private String mExpectedChallengeId = "";
    private String mExpectedRewardEventId = "";
    private String mExpectedGateEventId = "";
    private String mExpectedWorldEventId = "";
    private String mExpectedScoreEventId = "";
    private double mExpectedRecordValue = 0;

    // VirtualGood
    private static class VirtualItemBalanceEventExpectation {
        public final String ExpectedVirtualItemId;
        public final int ExpectedVirtualItemAmountAdded;
        public final int ExpectedVirtualItemBalance;

        private VirtualItemBalanceEventExpectation(String expectedVirtualItemId,
                                                   int expectedVirtualItemAmountAdded,
                                                   int expectedVirtualItemBalance) {
            ExpectedVirtualItemId = expectedVirtualItemId;
            ExpectedVirtualItemAmountAdded = expectedVirtualItemAmountAdded;
            ExpectedVirtualItemBalance = expectedVirtualItemBalance;
        }
    }

    private Queue<VirtualItemBalanceEventExpectation> mVirtualItemExpectationQueue =
        new ArrayDeque<VirtualItemBalanceEventExpectation>();

    @Before
    public void setUp() throws Exception {
        SoomlaApp.setExternalContext(Robolectric.application);//this line must be first
        Soomla.initialize("LevelUpTestSecret");
        BusProvider.getInstance().register(this);

        mVirtualItemExpectationQueue.clear();

        if(!firstSetupDone) {
            deleteDB();

            firstSetupDone = true;
        }

        SoomlaStore.getInstance().initialize(new IStoreAssets() {
            @Override
            public int getVersion() {
                return 1;
            }

            @Override
            public VirtualCurrency[] getCurrencies() {
                return new VirtualCurrency[0];
            }

            @Override
            public VirtualGood[] getGoods() {
                int i = 5;
                final VirtualGood[] virtualGoods = new VirtualGood[i];
                virtualGoods[--i] = new SingleUseVG("ItemBalanceGate",
                        "", ITEM_ID_BALANCE_GATE,
                        new PurchaseWithMarket(ITEM_ID_BALANCE_GATE, 1));
                virtualGoods[--i] = new SingleUseVG("ItemBalanceMission",
                        "", ITEM_ID_BALANCE_MISSION,
                        new PurchaseWithMarket(ITEM_ID_BALANCE_MISSION, 1));
                virtualGoods[--i] = new SingleUseVG("ItemBalanceMissionReward",
                        "", ITEM_ID_BALANCE_MISSION_REWARD,
                        new PurchaseWithMarket(ITEM_ID_BALANCE_MISSION_REWARD, 1));
                virtualGoods[--i] = new SingleUseVG("ItemVIScore",
                        "", ITEM_ID_VI_SCORE,
                        new PurchaseWithMarket(ITEM_ID_VI_SCORE, 1));
                virtualGoods[--i] = new SingleUseVG("ItemVIReward",
                        "", ITEM_ID_VI_REWARD,
                        new PurchaseWithMarket(ITEM_ID_VI_REWARD, 1));

                return virtualGoods;
            }

            @Override
            public VirtualCurrencyPack[] getCurrencyPacks() {
                return new VirtualCurrencyPack[0];
            }

            @Override
            public VirtualCategory[] getCategories() {
                return new VirtualCategory[0];
            }

            @Override
            public NonConsumableItem[] getNonConsumableItems() {
                int i = 2;
                final NonConsumableItem[] nonConsumableItems = new NonConsumableItem[i];
                nonConsumableItems[--i] = new NonConsumableItem("ItemPurchaseGateWithMarket",
                        "", ITEM_ID_PURCHASE_GATE_VI,
                        new PurchaseWithMarket(ITEM_ID_PURCHASE_GATE_VI, 10));
                nonConsumableItems[--i] = new NonConsumableItem("ItemPurchaseGateWithVI",
                        "", ITEM_ID_PURCHASE_GATE_MARKET,
                        new PurchaseWithMarket(ITEM_ID_PURCHASE_GATE_MARKET, 2));
                return nonConsumableItems;
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        // assert no expected events did not fire
        Assert.assertTrue(mVirtualItemExpectationQueue.isEmpty());

        BusProvider.getInstance().unregister(this);
    }

    @AfterClass
    public static void deleteDB() {
        System.out.println("==firstSetup==");
        // for some reason this doesn't work (even outside @AfterClass notation)
//        System.out.println("app.deleteDatabase:" +
//                Robolectric.application.deleteDatabase(ShadowSQLiteOpenHelper.DB_NAME));
        // clear database files manually
        System.out.println("delete db file:" +
                new File(ShadowSQLiteOpenHelper.DB_NAME).delete());
        System.out.println("delete db journal file:" +
                new File(ShadowSQLiteOpenHelper.DB_NAME + "-journal").delete());
    }

//    @Test
//    public void testLevelupFromJSON() {
//        String jsonFileContents = readFile("levelup.json");
//        JSONObject worldJson = null;
//        try {
//            worldJson = new JSONObject(jsonFileContents);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        World world = World.fromJSONObject(worldJson);
//
//    }

    @Test
    public void testLevel() {
        final List<World> worlds = new ArrayList<World>();
        Level lvl1 = new Level("lvl1");
        worlds.add(lvl1);

        LevelUp.getInstance().initialize(worlds);

        // no gates
        Assert.assertTrue(lvl1.canStart());
        Assert.assertTrue(lvl1.getState() == Level.State.Idle);

        mExpectedWorldEventId = "lvl1";

        lvl1.start();
        Assert.assertTrue(lvl1.getState() == Level.State.Running);

        sleep(1000);
        // check level time measure
        double playDuration = lvl1.getPlayDuration();
        System.out.println("playDuration = " + playDuration);
        Assert.assertTrue(playDuration >= 1);
        Assert.assertFalse(playDuration > 2);

        lvl1.pause();
        sleep(1000);
        // make sure no changes after pause
        playDuration = lvl1.getPlayDuration();
        System.out.println("playDuration = " + playDuration);
        Assert.assertTrue(playDuration >= 1);
        Assert.assertFalse(playDuration > 2);
        Assert.assertTrue(lvl1.getState() == Level.State.Paused);

        lvl1.resume();
        sleep(1000);
        // make sure working after resume
        playDuration = lvl1.getPlayDuration();
        System.out.println("playDuration = " + playDuration);
        Assert.assertTrue(playDuration >= 2);
        Assert.assertFalse(playDuration > 3);
        Assert.assertTrue(lvl1.getState() == Level.State.Running);

        lvl1.end(false);
        Assert.assertTrue(lvl1.getState() == Level.State.Ended);
        Assert.assertFalse(lvl1.isCompleted());

        lvl1.setCompleted(true);
        Assert.assertTrue(lvl1.isCompleted());

        Assert.assertEquals(playDuration, lvl1.getSlowestDuration(), 0.1);
        Assert.assertEquals(playDuration, lvl1.getFastestDuration(), 0.1);
        Assert.assertEquals(1, lvl1.getTimesPlayed());
        Assert.assertEquals(1, lvl1.getTimesStarted());
    }

    @Test
    public void testScoreAsc() {
        boolean higherIsBetter = true;
        final String scoreId = "score_asc";
        Score scoreAsc = new Score(scoreId, "ScoreAsc", higherIsBetter);

        mExpectedScoreEventId = scoreId;

        Assert.assertEquals(0, scoreAsc.getTempScore(), 0.01);
        scoreAsc.setStartValue(0);
        scoreAsc.inc(1);
        Assert.assertEquals(1, scoreAsc.getTempScore(), 0.01);
        scoreAsc.dec(1);
        Assert.assertEquals(0, scoreAsc.getTempScore(), 0.01);
        scoreAsc.inc(10);
        Assert.assertEquals(10, scoreAsc.getTempScore(), 0.01);
        mExpectedRecordValue = 10;
        scoreAsc.saveAndReset();
        Assert.assertEquals(10, scoreAsc.getLatest(), 0.01);
        Assert.assertEquals(0, scoreAsc.getTempScore(), 0.01);
        scoreAsc.setTempScore(20);
        mExpectedRecordValue = 0;
        scoreAsc.reset();
        Assert.assertEquals(0, scoreAsc.getLatest(), 0.01);
        Assert.assertEquals(0, scoreAsc.getTempScore(), 0.01);
        scoreAsc.setTempScore(30);
        Assert.assertTrue(scoreAsc.hasTempReached(30));
        Assert.assertFalse(scoreAsc.hasTempReached(31));
        mExpectedRecordValue = 30;
        scoreAsc.saveAndReset();
        Assert.assertEquals(30, scoreAsc.getLatest(), 0.01);
        Assert.assertEquals(30, scoreAsc.getRecord(), 0.01);
        scoreAsc.setTempScore(15);
        mExpectedRecordValue = 30;
        scoreAsc.saveAndReset();
        Assert.assertEquals(15, scoreAsc.getLatest(), 0.01);
        Assert.assertEquals(30, scoreAsc.getRecord(), 0.01);
        Assert.assertTrue(scoreAsc.hasRecordReached(30));
        Assert.assertFalse(scoreAsc.hasRecordReached(31));
    }

    @Test
    public void testScoreDsc() {
        boolean higherIsBetter = false;
        final String scoreId = "score_dsc";
        Score scoreDsc = new Score(scoreId, "ScoreDsc", !higherIsBetter);

        mExpectedScoreEventId = scoreId;

        // todo: I think reset behavior is a bug/confusing with dsc score
        // todo: it will set a latest+record of zero which cannot be broken
        // todo: later by positive numbers
        scoreDsc.reset();

        Assert.assertEquals(100, scoreDsc.getTempScore(), 0.01);
        scoreDsc.dec(50);
        Assert.assertEquals(50, scoreDsc.getTempScore(), 0.01);
        mExpectedRecordValue = 50;
        scoreDsc.saveAndReset(); // start value is 100
        Assert.assertEquals(50, scoreDsc.getLatest(), 0.01);
        Assert.assertEquals(100, scoreDsc.getTempScore(), 0.01);
        scoreDsc.setTempScore(20);
        mExpectedRecordValue = 20;
        scoreDsc.saveAndReset();
        Assert.assertEquals(20, scoreDsc.getLatest(), 0.01);
        Assert.assertEquals(20, scoreDsc.getRecord(), 0.01);
        scoreDsc.setTempScore(30);
        mExpectedRecordValue = 20;
        scoreDsc.saveAndReset();
        Assert.assertEquals(30, scoreDsc.getLatest(), 0.01);
        Assert.assertEquals(20, scoreDsc.getRecord(), 0.01);
        Assert.assertTrue(scoreDsc.hasRecordReached(20));
        Assert.assertFalse(scoreDsc.hasRecordReached(19));
    }

    @Test
    public void testRecordMission() {
        final String missionId = "record_mission";
        final String scoreId = "record_mission_score";
        final String rewardId = "record_mission_reward_badge_id";
        final double desiredScore = 55;
        final BadgeReward badgeReward = new BadgeReward(rewardId, "RecordMissionBadge");
        List<Reward> rewards = new ArrayList<Reward>();
        rewards.add(badgeReward);
        final Score score = new Score(scoreId, "RecordMissionScore", true);
        final RecordMission recordMission = new RecordMission(
                missionId, "RecordMission", rewards, scoreId, desiredScore);

        mExpectedScoreEventId = scoreId;
        mExpectedRecordValue = desiredScore;
        mExpectedMissionEventId = missionId;
        mExpectedRewardEventId = rewardId;

        Assert.assertFalse(recordMission.isCompleted());
        Assert.assertFalse(badgeReward.isOwned());

        score.setTempScore(desiredScore);
        score.saveAndReset();

        Assert.assertTrue(recordMission.isCompleted());
        Assert.assertTrue(badgeReward.isOwned());

        // test revoke
        recordMission.setCompleted(false);
    }

    @Test
    public void testBalanceMission() {
        final String missionId = "balance_mission_id";
        final String balanceMissionItemId = "balance_mission_item_id";
        final String rewardId = "balance_mission_reward_id";
        final String rewardItemId = "balance_mission_reward_item_id";

        final VirtualItemReward virtualItemReward = new VirtualItemReward(rewardId, "ItemReward", 1, rewardItemId);
        List<Reward> rewards = new ArrayList<Reward>();
        rewards.add(virtualItemReward);
        BalanceMission balanceMission = new BalanceMission(
                missionId, "BalanceMission",
                rewards, balanceMissionItemId, 5);

        // assert basics
        Assert.assertFalse(balanceMission.isCompleted());
        Assert.assertFalse(virtualItemReward.isOwned());
        try {
            Assert.assertEquals(0, StoreInventory.getVirtualItemBalance(ITEM_ID_BALANCE_MISSION));
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        // give less and assert false completed/rewarded
        // set event expectations
        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(ITEM_ID_BALANCE_MISSION, 3, 3));
        try {
            StoreInventory.giveVirtualItem(balanceMissionItemId, 3);
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        // set event expectations
        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(ITEM_ID_BALANCE_MISSION, 2, 5));
        // this will happen directly after
        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(ITEM_ID_BALANCE_MISSION_REWARD, 1, 1));

        mExpectedMissionEventId = missionId;
        mExpectedRewardEventId = rewardId;

        try {
            StoreInventory.giveVirtualItem(balanceMissionItemId, 2);
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(balanceMission.isCompleted());
        Assert.assertTrue(virtualItemReward.isOwned());
    }

    @Test
    public void testChallenge() {
        final String missionId1 = "challenge_mission1";
        final Mission mission1 = new ActionMission(missionId1, "ChallengeMission1");
        final String missionId2 = "challenge_mission2";
        final Mission mission2 = new ActionMission(missionId2, "ChallengeMission1");
        final List<Mission> missions = new ArrayList<Mission>();
        missions.add(mission1);
        missions.add(mission2);
        final List<Reward> rewards = new ArrayList<Reward>();
        final String rewardId = "challenge_badge_reward_id";
        final BadgeReward badgeReward = new BadgeReward(rewardId, "ChallengeBadgeRewardId");
        rewards.add(badgeReward);
        final String challengeId = "challenge_id";
        Challenge challenge = new Challenge(challengeId, "Challenge", missions, rewards);

        Assert.assertFalse(challenge.isCompleted());

        mExpectedMissionEventId = missionId1;

        mission1.setCompleted(true);

        mExpectedMissionEventId = missionId2;
        mExpectedChallengeId = challengeId;
        mExpectedRewardEventId = rewardId;

        mission2.setCompleted(true);

        Assert.assertTrue(challenge.isCompleted());
    }

    @Test
    public void testRewards() {
        boolean given;
//        BadgeReward badgeReward = new BadgeReward();
//        badgeReward.setRepeatable(false);
//        Assert.assertFalse(badgeReward.isOwned());
//        given = badgeReward.give();
//        Assert.assertTrue(given);
//        given = badgeReward.give();
//        Assert.assertFalse(given);
//        Assert.assertTrue(badgeReward.isOwned());
//
//        RandomReward randomReward = new RandomReward();
//        SequenceReward sequenceReward = new SequenceReward();

        final String rewardId = "vi_reward";
        VirtualItemReward virtualItemReward = new VirtualItemReward(rewardId, "VIReward", 3, ITEM_ID_VI_REWARD);
        virtualItemReward.setRepeatable(true);

        try {
            Assert.assertEquals(0, StoreInventory.getVirtualItemBalance(ITEM_ID_VI_REWARD));
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        // expected events (async)
        mExpectedRewardEventId = rewardId;
        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(ITEM_ID_VI_REWARD, 3, 3));

        given = virtualItemReward.give();
        Assert.assertTrue(given);

        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(ITEM_ID_VI_REWARD, 3, 6));

        given = virtualItemReward.give();
        Assert.assertTrue(given);

        try {
            Assert.assertEquals(6, StoreInventory.getVirtualItemBalance(ITEM_ID_VI_REWARD));
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testRecordGateWithRangeScore() {
        final List<World> worlds = new ArrayList<World>();
        final String lvl1Id = "lvl1_recordgate_rangescore";
        Level lvl1 = new Level(lvl1Id);
        final String lvl2Id = "lvl2_recordgate_rangescore";
        Level lvl2 = new Level(lvl2Id);
        final String scoreId = "range_score";
        final RangeScore rangeScore = new RangeScore(scoreId, "RangeScore", new RangeScore.Range(0, 100));
        final String recordGateId = "record_gate";
        final RecordGate recordGate = new RecordGate(recordGateId, scoreId, 100);
        lvl1.addScore(rangeScore);
        lvl2.addGate(recordGate);

        worlds.add(lvl1);
        worlds.add(lvl2);

        LevelUp.getInstance().initialize(worlds);

        // open level
        Assert.assertTrue(lvl1.canStart());
        // protected by gate
        Assert.assertFalse(lvl2.canStart());

        mExpectedWorldEventId = lvl1Id;

        lvl1.start();

        int i = 0;
        Assert.assertFalse(recordGate.isOpen());
        Assert.assertFalse(recordGate.canOpen());
        while (i < 100) {
            rangeScore.inc(1);
            ++i;
        }
        Assert.assertFalse(recordGate.isOpen());
        Assert.assertFalse(recordGate.canOpen());

        mExpectedGateEventId = recordGateId;
        mExpectedScoreEventId = scoreId;
        mExpectedRecordValue = 100;

        rangeScore.inc(1);

        lvl1.end(true);

        Assert.assertFalse(recordGate.isOpen());
        Assert.assertTrue(recordGate.canOpen());

        final boolean opened = recordGate.tryOpen();
        Assert.assertTrue(opened);
        Assert.assertTrue(recordGate.isOpen());
        Assert.assertTrue(recordGate.canOpen());

        Assert.assertTrue(lvl2.canStart());

        mExpectedWorldEventId = lvl2Id;

        lvl2.start();
        lvl2.end(true);

        Assert.assertTrue(lvl2.isCompleted());

        // test json serialization
//        final String json = KeyValueStorage.getValue(LevelUp.DB_KEY_PREFIX + "model");
//        writeFile("tests/levelup.json", json);
    }

    @Test
    public void testBalanceGate() {
        final List<World> worlds = new ArrayList<World>();
        final String lvl1Id = "lvl1_balancegate";
        Level lvl1 = new Level(lvl1Id);
        final String lvl2Id = "lvl2_balancegate";
        Level lvl2 = new Level(lvl2Id);
        final String itemId = ITEM_ID_BALANCE_GATE;
        final String balanceGateId = "balance_gate";

        final BalanceGate balanceGate = new BalanceGate(balanceGateId, itemId, 1);
        lvl2.addGate(balanceGate);

        worlds.add(lvl1);
        worlds.add(lvl2);

        LevelUp.getInstance().initialize(worlds);

        // open level
        Assert.assertTrue(lvl1.canStart());
        // protected by gate
        Assert.assertFalse(lvl2.canStart());

        // set up events expectations (async)
        mExpectedWorldEventId = lvl1Id;
        mExpectedGateEventId = balanceGateId;
        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(itemId, 1, 1));

        lvl1.start();

        Assert.assertFalse(balanceGate.isOpen());
        Assert.assertFalse(balanceGate.canOpen());

        try {
            StoreInventory.giveVirtualItem(itemId, 1);
            Assert.assertTrue(balanceGate.canOpen());

        } catch (VirtualItemNotFoundException e) {
            e.printStackTrace();
        }

        lvl1.end(true);

        Assert.assertFalse(balanceGate.isOpen());
        Assert.assertTrue(balanceGate.canOpen());

        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(itemId, -1, 0));

        final boolean opened = balanceGate.tryOpen();
        Assert.assertTrue(opened);
        Assert.assertTrue(balanceGate.isOpen());
        Assert.assertTrue(balanceGate.canOpen());

        Assert.assertTrue(lvl2.canStart());

        mExpectedWorldEventId = lvl2Id;

        lvl2.start();
        lvl2.end(true);

        Assert.assertTrue(lvl2.isCompleted());
    }

    @Test
    public void testWorldCompletionGate() {
        final List<World> worlds = new ArrayList<World>();
        final String lvl1Id = "lvl1_completiongate";
        Level lvl1 = new Level(lvl1Id);
        final String lvl2Id = "lvl2_completiongate";
        Level lvl2 = new Level(lvl2Id);
        final String worldGateId = "world_gate";

        final WorldCompletionGate lvl1CompletionGate =
                new WorldCompletionGate(worldGateId, lvl1Id);
        lvl2.addGate(lvl1CompletionGate);

        worlds.add(lvl1);
        worlds.add(lvl2);

        LevelUp.getInstance().initialize(worlds);

        // open level
        Assert.assertTrue(lvl1.canStart());
        // protected by gate
        Assert.assertFalse(lvl2.canStart());

        mExpectedWorldEventId = lvl1Id;

        lvl1.start();

        Assert.assertFalse(lvl1CompletionGate.isOpen());
        Assert.assertFalse(lvl1CompletionGate.canOpen());

        // set up events expectations (async)
        mExpectedGateEventId = worldGateId;

        lvl1.end(true);

        Assert.assertFalse(lvl1CompletionGate.isOpen());
        Assert.assertTrue(lvl1CompletionGate.canOpen());

        final boolean opened = lvl1CompletionGate.tryOpen();
        Assert.assertTrue(opened);
        Assert.assertTrue(lvl1CompletionGate.isOpen());
        Assert.assertTrue(lvl1CompletionGate.canOpen());

        Assert.assertTrue(lvl2.canStart());

        mExpectedWorldEventId = lvl2Id;

        lvl2.start();
        lvl2.end(true);

        Assert.assertTrue(lvl2.isCompleted());
    }

    @Theory
    public void testPurchasableGate(boolean vi) {
        final List<World> worlds = new ArrayList<World>();
        final String lvl1Id = "lvl1_purchasablegate";
        Level lvl1 = new Level(lvl1Id);
        Level lvl2 = new Level("lvl2_purchasablegate");
        final String itemId = vi ? ITEM_ID_PURCHASE_GATE_VI : ITEM_ID_PURCHASE_GATE_MARKET;
        final String purchaseGateId = vi ? "purchase_gate_vi": "purchase_gate_market";

        final PurchasableGate purchasableGate = new PurchasableGate(purchaseGateId, itemId);
        lvl2.addGate(purchasableGate);

        worlds.add(lvl1);
        worlds.add(lvl2);

        LevelUp.getInstance().initialize(worlds);

        // open level
        Assert.assertTrue(lvl1.canStart());
        // protected by gate
        Assert.assertFalse(lvl2.canStart());
        lvl1.start();

        Assert.assertFalse(purchasableGate.isOpen());
        Assert.assertFalse(purchasableGate.canOpen());

        lvl1.end(true);

        Assert.assertFalse(purchasableGate.isOpen());
        Assert.assertTrue(purchasableGate.canOpen());

        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(itemId, 10, 10));

        try {
            StoreInventory.giveVirtualItem(itemId, 10);
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(itemId, -10, 0));

        try {
            StoreInventory.buy(itemId);
        } catch (InsufficientFundsException e) {
            Assert.fail(e.getMessage());
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        final boolean opened = purchasableGate.tryOpen();
        Assert.assertTrue(opened);
        Assert.assertTrue(purchasableGate.isOpen());
        Assert.assertTrue(purchasableGate.canOpen());

        Assert.assertTrue(lvl2.canStart());
        lvl2.start();
        lvl2.end(true);

        Assert.assertTrue(lvl2.isCompleted());
    }

    @Test
    public void testGatesList() {
        final String recordGateId1 = "gates_list_record_gate_id1";
        final String scoreId1 = "gates_list_score_id1";
        final double desiredRecord1 = 10;
        final String recordGateId2 = "gates_list_record_gate_id2";
        final String scoreId2 = "gates_list_score_id2";
        final double desiredRecord2 = 20;

        Score score1 = new Score(scoreId1, "GatesListScore1", true);
        Score score2 = new Score(scoreId2, "GatesListScore2", true);

        final List<World> worlds = new ArrayList<World>();
        final String lvl1Id = "lvl1_gates_list";
        Level lvl1 = new Level(lvl1Id);
        lvl1.addScore(score1);
        lvl1.addScore(score2);
        worlds.add(lvl1);

        RecordGate recordGate1 = new RecordGate(recordGateId1, scoreId1, desiredRecord1);
        RecordGate recordGate2 = new RecordGate(recordGateId2, scoreId2, desiredRecord2);

        List<Gate> gates = new ArrayList<Gate>();
        gates.add(recordGate1);
        gates.add(recordGate2);

        final String gateListORId = "gate_list_OR_id";
        GatesListOR gatesListOR = new GatesListOR(gateListORId, gates);

        final String gateListANDId = "gate_list_AND_id";
        GatesListAND gatesListAND = new GatesListAND(gateListANDId, gates);

        LevelUp.getInstance().initialize(worlds);

        mExpectedGateEventId = recordGateId1;
        mExpectedScoreEventId = scoreId1;
        mExpectedRecordValue = desiredRecord1;

        score1.setTempScore(desiredRecord1);
        score1.saveAndReset();

        Assert.assertTrue(recordGate1.canOpen());
        Assert.assertFalse(recordGate1.isOpen());

        Assert.assertTrue(recordGate1.tryOpen());

        Assert.assertTrue(gatesListOR.canOpen());
        // todo: could be confusing, no need to tryOpen it
        Assert.assertTrue(gatesListOR.isOpen());

        Assert.assertFalse(gatesListAND.canOpen());
        Assert.assertFalse(gatesListAND.isOpen());

        mExpectedGateEventId = gateListORId;

        // todo: could be confusing, no need to tryOpen it
        Assert.assertTrue(gatesListOR.tryOpen());

        mExpectedGateEventId = recordGateId2;
        mExpectedScoreEventId = scoreId2;
        mExpectedRecordValue = desiredRecord2;

        score2.setTempScore(desiredRecord2);
        score2.saveAndReset();

        Assert.assertTrue(recordGate2.canOpen());
        Assert.assertFalse(recordGate2.isOpen());

        Assert.assertTrue(recordGate2.tryOpen());

        Assert.assertTrue(gatesListOR.canOpen());
        Assert.assertTrue(gatesListOR.isOpen());

        Assert.assertTrue(gatesListAND.canOpen());
        // todo: could be confusing, no need to tryOpen it
        Assert.assertTrue(gatesListAND.isOpen());

        mExpectedGateEventId = gateListANDId;

        // todo: could be confusing, no need to tryOpen it
        Assert.assertTrue(gatesListOR.tryOpen());
        Assert.assertTrue(gatesListAND.isOpen());
    }

    @Test
    public void testVirtualItemScore() {
        final List<World> worlds = new ArrayList<World>();
        final String lvl1Id = "lvl1_viscore";
        Level lvl1 = new Level(lvl1Id);
        final String itemId = ITEM_ID_VI_SCORE;
        final String scoreId = "vi_score";
        final VirtualItemScore virtualItemScore = new VirtualItemScore(
                scoreId, "VI_Score", itemId);
        lvl1.addScore(virtualItemScore);

        worlds.add(lvl1);

        LevelUp.getInstance().initialize(worlds);

        try {
            Assert.assertEquals(0, StoreInventory.getVirtualItemBalance(itemId));
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        // set up events expectations (async)
        mExpectedWorldEventId = lvl1Id;
        mVirtualItemExpectationQueue.add(new VirtualItemBalanceEventExpectation(itemId, 2, 2));
        mExpectedScoreEventId = scoreId;
        mExpectedRecordValue = 2;

        lvl1.start();
        virtualItemScore.inc(2);
        lvl1.end(true);

        try {
            Assert.assertEquals(2, StoreInventory.getVirtualItemBalance(itemId));
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Subscribe
    public void onEvent(GateCanBeOpenedEvent gateCanBeOpenedEvent) {
        final String gateId = gateCanBeOpenedEvent.Gate.getGateId();
        System.out.println("onEvent/GateCanBeOpenedEvent:" + gateId);
        Assert.assertEquals(mExpectedGateEventId, gateId);
    }

    @Subscribe
    public void onEvent(GateOpenedEvent gateOpenedEvent) {
        final String gateId = gateOpenedEvent.Gate.getGateId();
        System.out.println("onEvent/GateOpenedEvent:" + gateId);
        Assert.assertEquals(mExpectedGateEventId, gateId);
    }

    @Subscribe
    public void onEvent(GoodBalanceChangedEvent goodBalanceChangedEvent) {
        final String itemId = goodBalanceChangedEvent.getGood().getItemId();
        System.out.println("onEvent/GoodBalanceChangedEvent:" + itemId);

        Assert.assertFalse(mVirtualItemExpectationQueue.isEmpty());
        VirtualItemBalanceEventExpectation expectation = mVirtualItemExpectationQueue.remove();

        Assert.assertEquals(expectation.ExpectedVirtualItemId, itemId);
        Assert.assertEquals(expectation.ExpectedVirtualItemAmountAdded, goodBalanceChangedEvent.getAmountAdded());
        Assert.assertEquals(expectation.ExpectedVirtualItemBalance, goodBalanceChangedEvent.getBalance());
    }

    @Subscribe
    public void onEvent(LevelStartedEvent levelStartedEvent) {
        final String worldId = levelStartedEvent.Level.getWorldId();
        System.out.println("onEvent/LevelStartedEvent:" + worldId);
        Assert.assertEquals(mExpectedWorldEventId, worldId);
    }

    @Subscribe
    public void onEvent(LevelEndedEvent levelEndedEvent) {
        final String worldId = levelEndedEvent.Level.getWorldId();
        System.out.println("onEvent/LevelEndedEvent:" + worldId);
        Assert.assertEquals(mExpectedWorldEventId, worldId);
    }

    @Subscribe
    public void onEvent(MissionCompletedEvent missionCompletedEvent) {
        final String missionId = missionCompletedEvent.Mission.getMissionId();
        System.out.println("onEvent/MissionCompletedEvent:" + missionId
                + "[challenge:" + missionCompletedEvent.IsChallenge + "]");

        final String expectedMissionId = missionCompletedEvent.IsChallenge ?
                mExpectedChallengeId : mExpectedMissionEventId;
        Assert.assertEquals(expectedMissionId, missionId);
    }

    @Subscribe
    public void onEvent(MissionCompletionRevokedEvent missionCompletionRevokedEvent) {
        final String missionId = missionCompletionRevokedEvent.Mission.getMissionId();
        System.out.println("onEvent/MissionCompletionRevokedEvent:" + missionId);
        Assert.assertEquals(mExpectedMissionEventId, missionId);
    }

    @Subscribe
    public void onEvent(ScoreRecordChangedEvent scoreRecordChangedEvent) {
        final String scoreId = scoreRecordChangedEvent.Score.getScoreId();
        final double record = scoreRecordChangedEvent.Score.getRecord();
        System.out.println("onEvent/ScoreRecordChangedEvent:" + scoreId + "->" + record);
        Assert.assertEquals(mExpectedScoreEventId, scoreId);
        Assert.assertEquals(mExpectedRecordValue, record, 0.01);
    }

    @Subscribe
    public void onEvent(WorldCompletedEvent worldCompletedEvent) {
        final String worldId = worldCompletedEvent.World.getWorldId();
        System.out.println("onEvent/WorldCompletedEvent:" + worldId);
        Assert.assertEquals(mExpectedWorldEventId, worldId);
    }

    @Subscribe
    public void onEvent(RewardGivenEvent rewardGivenEvent) {
        final String rewardId = rewardGivenEvent.Reward.getRewardId();
        System.out.println("onEvent/RewardGivenEvent:" + rewardId);
        Assert.assertEquals(mExpectedRewardEventId, rewardId);
    }

    @Subscribe
    public void onEvent(RewardTakenEvent rewardTakenEvent) {
        final String rewardId = rewardTakenEvent.Reward.getRewardId();
        System.out.println("onEvent/RewardTakenEvent:" + rewardId);
        Assert.assertEquals(mExpectedRewardEventId, rewardId);
    }

    public static String readFile(String filePath) {
        String text = null;
        try {
            InputStream is = new FileInputStream(new File(filePath));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            text = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return text;
    }

    public static void writeFile(String filePath, String contents) {
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath), "utf-8"));
            writer.write(contents);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
