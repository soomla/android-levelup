*This project is a part of [The SOOMLA Project](http://project.soom.la) which is a series of open source initiatives with a joint goal to help mobile game developers get better stores and more in-app purchases.*

<!-- insert short code teaser if you have it
Haven't you ever wanted an in-app purchase one liner that looks like this!?

```Java
    StoreInventory.buy("[itemId]");
```
end short code teaser -->

## android-levelup

<!-- updates when you have any -->
<!-- end updates -->

android-levelup is an open code initiative as part of The SOOMLA Project. It is a Java API that organizes and simplifies structuring of games for user progress.

It acts as sort of a 'blueprint' for the game, modeling worlds/levels, gates to levels, missions and rewards that can be completed and achieved.
All this is backed by Soomla's core tools, and can be easily integrated with more Soomla modules, like android-store for IAP, or android-profile for social related functions.


<!-- Check out our [Wiki] (https://github.com/soomla/android-store/wiki) for more information about the project and how to use it better. -->

## Model Overview

<!-- attach UML style simple diagram -->

Generally, the Soomla sources contain detailed documentation on the different entities and how to use them, but here's a quick glance:

** World/Level **

A _Level_ is pretty clear, and most games have them.
A simple example is an Angry Birds single level, where you need to knock out all the pigs.
It measures specific things, such as duration it takes to complete, and can be started and ended.


A _World_ is a more general concept than a Level (a Level **Is-a** World), and can have innerWorlds to create hierarchies. Another example from Angry Birds is level pages and episodes, which contain the actual levels.

** Score **

A _Score_ is something which can be accumulated or measured within a _World_ (or _Level_ of course).
It can be incremented or decremented based on user actions, and recorded at the completion of the _World/Level_.

This, in turn, can later be applied to high scores or best times, or treated as collectibles that can be awared upon completion.

** Gate **

A _Gate_ is closed portal from one _World_ to the next. It can be unlocked in many different ways (according to Gate type), and can also be combined into _GatesList_ to build more complex _Gates_.

** Mission/Challenge **

A _Mission_ is a single task a player can complete in a game, usually for a _Reward_.

A _Challenge_ is a set of _Missions_ that need to be completed, so it's a big _Mission_ built out of several smaller _Missions_.

** Reward **

A _Reward_ is some kind of perk or status a player can achieve in the game.
This can be either a badge, a virtual item from the game's economy (sword, coins etc.) or anything you can think of, really (unlocking game content or levels comes to mind).

## Getting Started (With sources)

1. Clone android-levelup. Copy all files from android-levelup/SoomlaAndroidLevelUp subfolders to their equivalent folders in your Android project:

 `git clone https://github.com/soomla/android-levelup`

2. Initialize Soomla using your secret key.
This will encrypt any sensitive local data Soomla will store on the device:

  ```Java
  Soomla.initialize(["YOUR CUSTOM GAME SECRET HERE"])
  ```
3. Initialize Soomla LevelUp, using your own implementation of your game structure:

  ```Java
    List<World> worlds = ... // (See example below)
    LevelUp.getInstance().initialize(worlds);
  ```

    > Soon we will support a json definitions file for the structure.


And that's it ! You now have access to all your game progress modeling abilities.


Here is an example:

Lets say we have 2 _Levels_ we call `lvl1` and `lvl2`. `lvl1` is open to start the game, but `lvl2` can only be unlocked by a market purchase:

```Java
  List<World> worlds = new ArrayList<World>();
  Level lvl1 = new Level("lvl1");
  Level lvl2 = new Level("lvl2");
  PurchasableGate purchaseGate = new   PurchasableGate("gate_id_lvl2", "item_id_lvl2_gate");
  lvl2.addGate(purchaseGate);
  worlds.add(lvl1);
  worlds.add(lvl2);
  LevelUp.getInstance().initialize(worlds);
```

Now `lvl2` is protected by the gate, and cannot `start()` until that item is purchased.
The item can be purchased using Soomla's _StoreInventory_ :

```Java
  StoreInventory.buy("item_id_lvl2_gate");
```

The item id `"item_id_lvl2_gate"` should also be defined in Soomla's `IStoreAssets`.
For more details on integrating with Soomla's IAP APIs (android-store) please see next section.

## Integration with Soomla android-store

Please follow steps in [android-store](https://github.com/soomla/android-store) for the _Store_ part of the setup.
Then, you can use the **store-related _LevelUp_ classes**, such as _VirtualItemScore_ or _VirtualItemReward_.

## Integration with Soomla android-profile
**(coming soon)**

Please follow steps in [android-profile](https://github.com/soomla/android-profile) for the _Profile_ part of the setup.
Then, you can use the **profile-related _LevelUp_ classes**, such as _SocialMission_.


## Debugging

In order to debug android-store, set `SoomlaConfig.logDebug` to `true`. This will print all of _android-store's_ debugging messages to logcat.

## Storage & Meta-Data

The on-device storage is encrypted and kept in a SQLite database. SOOMLA is preparing a cloud-based storage service that will allow this SQLite to be synced to a cloud-based repository that you'll define.

## Example Usages
**check out the `tests` branch for more examples and updates on these**

  > Examples using virtual items are dependent on android-store module, with proper SoomlaStore initialization and IStoreAssets definitions. See the android-store integration section for more details.

* Mission with Reward (collect 5 stars to get 1 mega star)

```Java
VirtualItemReward virtualItemReward = new VirtualItemReward("mega_star_reward_id", "MegaStarReward", 1, megaStarItemId);
List<Reward> rewards = new ArrayList<Reward>();
rewards.add(virtualItemReward);
BalanceMission balanceMission = new BalanceMission("star_balance_mission_id", "StarBalanceMission", rewards, "star", 5);

// use the store to give the items out, usually this will be called from in-game events
// such as player collecting the stars
StoreInventory.giveVirtualItem(starItemId, 5);

// events posted:
// 1. GoodBalanceChangedEvent
// 2. MissionCompletedEvent
// 3. RewardGivenEvent

// now the mission is complete, and reward given
balanceMission.isCompleted(); // true
virtualItemReward.isOwned(); // true

```

* RecordGate with RangeScore

```Java
List<World> worlds = new ArrayList<World>();
String lvl1Id = "lvl1_recordgate_rangescore";
Level lvl1 = new Level(lvl1Id);
Level lvl2 = new Level("lvl2_recordgate_rangescore");
String scoreId = "range_score";
RangeScore rangeScore = new RangeScore(scoreId, "RangeScore", new RangeScore.Range(0, 100));
String recordGateId = "record_gate";
RecordGate recordGate = new RecordGate(recordGateId, scoreId, 100);
lvl1.addScore(rangeScore);
lvl2.addGate(recordGate);

worlds.add(lvl1);
worlds.add(lvl2);

LevelUp.getInstance().initialize(worlds);

lvl1.start();

// LevelStartedEvent

rangeScore.inc(100);

// events posted:
// GateCanBeOpenedEvent

lvl1.end(true);

// events posted:
// LevelEndedEvent
// WorldCompletedEvent (lvl1)
// [ScoreRecordChangedEvent] - if record was broken

recordGate.canOpen(); // true
recordGate.isOpen(); // false, didn't try to open yet

boolean opened = recordGate.tryOpen(); // opened == true

// events posted:
// GateOpenedEvent

recordGate.isOpen(); // true
recordGate.canOpen(); // true

lvl2.canStart(); // true
lvl2.start();
lvl2.end(true);

// events posted:
// WorldCompletedEvent (lvl2)

lvl2.isCompleted(); // true
```

* VirtualItemScore

```Java
List<World> worlds = new ArrayList<World>();
String lvl1Id = "lvl1_viscore";
Level lvl1 = new Level(lvl1Id);
String itemId = ITEM_ID_VI_SCORE;
String scoreId = "vi_score";
VirtualItemScore virtualItemScore = new VirtualItemScore(
        scoreId, "VI_Score", itemId);
lvl1.addScore(virtualItemScore);

worlds.add(lvl1);

LevelUp.getInstance().initialize(worlds);

// set up events expectations
mExpectedVirtualItemId = itemId;
mExpectedVirtualItemAmountAdded = 2;
mExpectedVirtualItemBalance = 2;

mExpectedWorldEventId = lvl1Id;

lvl1.start();
// LevelStartedEvent

virtualItemScore.inc(2);

// GoodBalanceChangedEvent

lvl1.end(true);

// events posted:
// LevelEndedEvent
// WorldCompletedEvent (lvl1)
// [ScoreRecordChangedEvent] - if record was broken

try {
    Assert.assertEquals(2, StoreInventory.getVirtualItemBalance(itemId));
} catch (VirtualItemNotFoundException e) {
    Assert.fail(e.getMessage());
}
```

* Challenge (Multi-Mission)

```Java
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

challenge.isCompleted(); //false

mission1.setCompleted(true);

// events:
// MissionCompleteEvent (mission1)

mission2.setCompleted(true);

// events:
// MissionCompleteEvent (mission2)
// MissionCompleteEvent (challenge)
// RewardGivenEvent (badgeReward)

challenge.isCompleted(); // true

// revoke

mission1.setCompleted(false);

// events:
// MissionCompletionRevokedEvent (mission1)
// MissionCompletionRevokedEvent (challenge)
// RewardTakenEvent (badgeReward)

challenge.isCompleted(); // false
badgeReward.isOwned(); // false
```

* GatesList
> Note that currently a `GatesList` gate is automatically opened when sub-gates fulfill the `GatesList` requirement. This is different from a single `Gate`, which has a `canOpen` state separate from `isOpen`

```Java
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

score1.setTempScore(desiredRecord1);
score1.saveAndReset();

recordGate1.canOpen(); // true
recordGate1.isOpen(); // false

recordGate1.tryOpen(); // should succeed (and return true)

gatesListOR.canOpen(); // true (at least one sub-gate is open)
gatesListOR.isOpen(); // false
gatesListOR.tryOpen(); // should succeed (and return true)

gatesListAND.canOpen(); // false (all sub-gates need to be open for AND)
gatesListAND.isOpen(); // false


score2.setTempScore(desiredRecord2);
score2.saveAndReset();

recordGate2.canOpen(); // true
recordGate2.isOpen(); // false

recordGate2.tryOpen(); // should succeed (and return true)

gatesListOR.canOpen(); // still true
gatesListOR.isOpen(); // still true

gatesListAND.canOpen(); // true
gatesListAND.isOpen(); // false

gatesListOR.tryOpen(); // should succeed (and return true)
gatesListAND.isOpen(); // true

// event sequence for this example:
ScoreRecordChangedEvent:gates_list_score_id1->100.0
GateCanBeOpenedEvent:gates_list_record_gate_id1[list=false]
GateOpenedEvent:gates_list_record_gate_id1[list=false]
GateCanBeOpenedEvent:gate_list_OR_id[list=true]
GateOpenedEvent:gate_list_OR_id[list=true]
ScoreRecordChangedEvent:gates_list_score_id2->200.0
GateCanBeOpenedEvent:gates_list_record_gate_id2[list=false]
GateOpenedEvent:gates_list_record_gate_id2[list=false]
GateCanBeOpenedEvent:gate_list_AND_id[list=true]
GateOpenedEvent:gate_list_AND_id[list=true]

```


## Security


If you want to protect your game from 'bad people' (and who doesn't?!), you might want to follow some guidelines:

+ SOOMLA keeps the game's data in an encrypted database. In order to encrypt your data, SOOMLA generates a private key out of several parts of information. The Custom Secret is one of them. SOOMLA recommends that you provide this value when initializing with `Soomla.initialize()` and before you release your game. BE CAREFUL: You can change this value once! If you try to change it again, old data from the database will become unavailable.
+ Following Google's recommendation, SOOMLA also recommends that you split your public key and construct it on runtime or even use bit manipulation on it in order to hide it. The key itself is not secret information but if someone replaces it, your application might get fake messages that might harm it.

## Event Handling


For event handling, we use Square's great open-source project [otto](http://square.github.com/otto/). In ordered to be notified of store related events, you can register for specific events and create your game-specific behavior to handle them.

> Your behavior is an addition to the default behavior implemented by SOOMLA. You don't replace SOOMLA's behavior.

In order to register for events:

1. In the class that should receive the event create a function with the annotation '@Subscribe'. Example:

    ```Java
    @Subscribe public void onLevelEndedEvent(LevelEndedEvent levelEndedEvent) {
        ...
    }
    ```

2. You'll also have to register your class in the event bus (and unregister when needed):

   ```Java
   BusProvider.getInstance().register(this);
   ```

   ```Java
   BusProvider.getInstance().unregister(this);
   ```

> If your class is an Activity, register in 'onResume' and unregister in 'onPause'

You can find a full event handler example [here](https://github.com/soomla/android-store/blob/master/SoomlaAndroidExample/src/com/soomla/example/ExampleEventHandler.java).

[List of events](https://github.com/soomla/android-store/tree/master/SoomlaAndroidStore/src/com/soomla/store/events)

[Full documentation and explanation of otto](http://square.github.com/otto/)

## Contribution


We want you!

Fork -> Clone -> Implement -> Insert Comments -> Test -> Pull-Request.

We have great RESPECT for contributors.

## Code Documentation


android-levelup follows strict code documentation conventions. If you would like to contribute please read our [Documentation Guidelines](https://github.com/soomla/android-store/blob/master/documentation.md) and follow them. Clear, consistent  comments will make our code easy to understand.

## SOOMLA, Elsewhere ...


+ [Framework Page](http://project.soom.la/)
+ [On Facebook](https://www.facebook.com/pages/The-SOOMLA-Project/389643294427376)
+ [On AngelList](https://angel.co/the-soomla-project)

## License

Apache License. Copyright (c) 2012-2014 SOOMLA. http://project.soom.la
+ http://opensource.org/licenses/Apache-2.0
