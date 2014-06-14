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

import com.soomla.levelup.events.GateCanBeOpenedEvent;
import com.soomla.levelup.gates.BalanceGate;
import com.soomla.levelup.gates.RecordGate;
import com.soomla.levelup.scoring.RangeScore;
import com.soomla.levelup.scoring.VirtualItemScore;
import com.soomla.store.BusProvider;
import com.soomla.store.IStoreAssets;
import com.soomla.store.SoomlaApp;
import com.soomla.store.StoreController;
import com.soomla.store.StoreInventory;
import com.soomla.store.domain.NonConsumableItem;
import com.soomla.store.domain.VirtualCategory;
import com.soomla.store.domain.virtualCurrencies.VirtualCurrency;
import com.soomla.store.domain.virtualCurrencies.VirtualCurrencyPack;
import com.soomla.store.domain.virtualGoods.SingleUseVG;
import com.soomla.store.domain.virtualGoods.VirtualGood;
import com.soomla.store.events.GoodBalanceChangedEvent;
import com.soomla.store.exceptions.VirtualItemNotFoundException;
import com.soomla.store.purchaseTypes.PurchaseWithMarket;
import com.squareup.otto.Subscribe;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oriargov on 6/12/14.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LevelUpTest {

    public static final String ITEM_ID_BALANCE_GATE = "item_balance_gate";
    public static final String ITEM_ID_VI_SCORE = "item_vi_score";

    /** event expectations **/

    // Gate
    private String mExpectedGateEventId = "";

    // VirtualGood
    private String mExpectedVirtualItemId = "";
    private int mExpectedVirtualItemAmountAdded = -1;
    private int mExpectedVirtualItemBalance = -1;

    @Before
    public void setUp() throws Exception {
        SoomlaApp.setExternalContext(Robolectric.application);
        BusProvider.getInstance().register(this);

        StoreController.getInstance().initialize(new IStoreAssets() {
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
                final VirtualGood[] virtualGoods = new VirtualGood[2];
                virtualGoods[0] = new SingleUseVG("ItemBalanceGate",
                        "", ITEM_ID_BALANCE_GATE,
                        new PurchaseWithMarket(ITEM_ID_BALANCE_GATE, 1));
                virtualGoods[1] = new SingleUseVG("ItemVIScore",
                        "", ITEM_ID_VI_SCORE,
                        new PurchaseWithMarket(ITEM_ID_VI_SCORE, 1));
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
                return new NonConsumableItem[0];
            }
        }, "a", "b");
    }

    @After
    public void tearDown() throws Exception {
        BusProvider.getInstance().unregister(this);
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

    /**
     * todo: a problem in robolectric causes sqlite errors
     * when running several tests per class (normal usage)
     * the reason is the sqlite DB files being deleted between tests
     * while the SqliteOpenHelper remains the same object
     */
    @Test
    public void testAll() {
        testLevel();
        testRecordGate();
        testBalanceGate();
        testVirtualItemScore();
    }

    public void testLevel() {
        final List<World> worlds = new ArrayList<World>();
        Level lvl1 = new Level("lvl1");
        worlds.add(lvl1);

        LevelUp.getInstance().initialize(worlds);

        // no gates
        Assert.assertTrue(lvl1.canStart());
        Assert.assertTrue(lvl1.getState() == Level.State.Idle);

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

    public void testRecordGate() {
        final List<World> worlds = new ArrayList<World>();
        Level lvl1 = new Level("lvl1");
        Level lvl2 = new Level("lvl2");
        final String scoreId = "range_score";
        final RangeScore rangeScore = new RangeScore(scoreId, "RangeScore", new RangeScore.Range(0, 100));
        final RecordGate recordGate = new RecordGate("record_gate", scoreId, 100);
        lvl1.addScore(rangeScore);
        lvl2.addGate(recordGate);

        worlds.add(lvl1);
        worlds.add(lvl2);

        LevelUp.getInstance().initialize(worlds);

        // open level
        Assert.assertTrue(lvl1.canStart());
        // protected by gate
        Assert.assertFalse(lvl2.canStart());
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

        mExpectedGateEventId = "record_gate";

        rangeScore.inc(1);

        lvl1.end(true);

        Assert.assertFalse(recordGate.isOpen());
        Assert.assertTrue(recordGate.canOpen());

        final boolean opened = recordGate.tryOpen();
        Assert.assertTrue(opened);
        Assert.assertTrue(recordGate.isOpen());
        Assert.assertTrue(recordGate.canOpen());

        Assert.assertTrue(lvl2.canStart());
        lvl2.start();
        lvl2.end(true);

        Assert.assertTrue(lvl2.isCompleted());

        // test json serialization
//        final String json = StorageManager.getKeyValueStorage().getValue(LevelUp.DB_KEY_PREFIX + "model");
//        writeFile("tests/levelup.json", json);
    }

    public void testBalanceGate() {
        final List<World> worlds = new ArrayList<World>();
        Level lvl1 = new Level("lvl1");
        Level lvl2 = new Level("lvl2");
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
        lvl1.start();

        Assert.assertFalse(balanceGate.isOpen());
        Assert.assertFalse(balanceGate.canOpen());

        // set up events expectations (async)
        mExpectedGateEventId = balanceGateId;
        mExpectedVirtualItemId = itemId;
        mExpectedVirtualItemAmountAdded = 1;
        mExpectedVirtualItemBalance = 1;

        try {
            StoreInventory.giveVirtualItem(itemId, 1);
            Assert.assertTrue(balanceGate.canOpen());

        } catch (VirtualItemNotFoundException e) {
            e.printStackTrace();
        }

        lvl1.end(true);

        Assert.assertFalse(balanceGate.isOpen());
        Assert.assertTrue(balanceGate.canOpen());

        mExpectedVirtualItemId = itemId;
        mExpectedVirtualItemAmountAdded = -1;
        mExpectedVirtualItemBalance = 0;

        final boolean opened = balanceGate.tryOpen();
        Assert.assertTrue(opened);
        Assert.assertTrue(balanceGate.isOpen());
        Assert.assertTrue(balanceGate.canOpen());

        Assert.assertTrue(lvl2.canStart());
        lvl2.start();
        lvl2.end(true);

        Assert.assertTrue(lvl2.isCompleted());
    }

    public void testVirtualItemScore() {
        final List<World> worlds = new ArrayList<World>();
        Level lvl1 = new Level("lvl1");
        final String itemId = ITEM_ID_VI_SCORE;
        final VirtualItemScore virtualItemScore = new VirtualItemScore(
                "vi_score", "VI_Score", itemId);
        lvl1.addScore(virtualItemScore);

        worlds.add(lvl1);

        LevelUp.getInstance().initialize(worlds);

        try {
            Assert.assertEquals(StoreInventory.getVirtualItemBalance(itemId), 0);
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        // set up events expectations (async)
        mExpectedVirtualItemId = itemId;
        mExpectedVirtualItemAmountAdded = 2;
        mExpectedVirtualItemBalance = 2;

        lvl1.start();
        virtualItemScore.inc(2);
        lvl1.end(true);

        try {
            Assert.assertEquals(StoreInventory.getVirtualItemBalance(itemId), 2);
        } catch (VirtualItemNotFoundException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Subscribe
    public void onEvent(GateCanBeOpenedEvent gateCanBeOpenedEvent) {
        System.out.println("onEvent/GateCanBeOpenedEvent:" + gateCanBeOpenedEvent.Gate.getGateId());
        Assert.assertEquals(gateCanBeOpenedEvent.Gate.getGateId(), mExpectedGateEventId);
    }

    @Subscribe
    public void onEvent(GoodBalanceChangedEvent goodBalanceChangedEvent) {
        System.out.println("onEvent/GoodBalanceChangedEvent:" + goodBalanceChangedEvent.getGood().getItemId());
        Assert.assertEquals(goodBalanceChangedEvent.getGood().getItemId(), mExpectedVirtualItemId);
        Assert.assertEquals(goodBalanceChangedEvent.getAmountAdded(), mExpectedVirtualItemAmountAdded);
        Assert.assertEquals(goodBalanceChangedEvent.getBalance(), mExpectedVirtualItemBalance);
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
