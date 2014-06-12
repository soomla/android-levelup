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
import com.soomla.levelup.gates.RecordGate;
import com.soomla.levelup.scoring.RangeScore;
import com.soomla.store.BusProvider;
import com.soomla.store.SoomlaApp;
import com.soomla.store.data.StorageManager;
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

    private String mExpectedGateEventId = "";

    @Before
    public void setUp() throws Exception {
        SoomlaApp.setExternalContext(Robolectric.application);
        BusProvider.getInstance().register(this);
    }

    @After
    public void tearDown() throws Exception {
        mExpectedGateEventId = "";
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

    @Test public void testLevelupToJSON() {
        final List<World> worlds = new ArrayList<World>();
        Level lvl1 = new Level("lvl1");
        Level lvl2 = new Level("lvl2");
        final RangeScore rangeScore = new RangeScore("score1", "RangeScore", new RangeScore.Range(0, 100));
        final RecordGate recordGate = new RecordGate("record_gate", "score1", 100);
        lvl1.addScore(rangeScore);
        lvl2.addGate(recordGate);

        worlds.add(lvl1);
        worlds.add(lvl2);
        LevelUp.getInstance().initialize(worlds);

        Assert.assertTrue(lvl1.canStart());
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

//        final BalanceGate balanceGate = new BalanceGate("balance_gate", "item1", 1);
//        lvl2.addGate(balanceGate);
//        try {
//            StoreInventory.giveVirtualItem("item1", 1);
//            Assert.assertTrue(balanceGate.canOpen());
//
//        } catch (VirtualItemNotFoundException e) {
//            e.printStackTrace();
//        }

        // test json serialization
        LevelUp.getInstance().initialize(worlds);
        final String json = StorageManager.getKeyValueStorage().getValue(LevelUp.DB_KEY_PREFIX + "model");
        writeFile("tests/levelup.json", json);
    }

    @Subscribe
    public void onEvent(GateCanBeOpenedEvent gateCanBeOpenedEvent) {
        System.out.println("onEvent/GateCanBeOpenedEvent:" + gateCanBeOpenedEvent.Gate.getGateId());
        Assert.assertEquals(gateCanBeOpenedEvent.Gate.getGateId(), mExpectedGateEventId);
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
}
