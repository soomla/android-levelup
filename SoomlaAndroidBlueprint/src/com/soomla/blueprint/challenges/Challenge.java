package com.soomla.blueprint.challenges;

import com.soomla.blueprint.data.BPJSONConsts;
import com.soomla.blueprint.events.MissionCompletedEvent;
import com.soomla.blueprint.rewards.Reward;
import com.soomla.store.BusProvider;
import com.soomla.store.StoreUtils;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by refaelos on 13/05/14.
 */
public class Challenge extends Mission {
    private static final String TAG = "SOOMLA Challenge";
    private List<Mission> mMissions;

    public Challenge(String missionId, String name, List<Mission> missions) {
        super(name, missionId);
        mMissions = missions;
        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public Challenge(String missionId, String name, List<Mission> missions, List<Reward> rewards) {
        super(missionId, name, rewards);
        mMissions = missions;

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public Challenge(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mMissions = new ArrayList<Mission>();
        JSONArray missionsArr = jsonObject.getJSONArray(BPJSONConsts.BP_MISSIONS);
        for (int i=0; i<missionsArr.length(); i++) {
            JSONObject missionJSON = missionsArr.getJSONObject(i);
            String type = missionJSON.getString(BPJSONConsts.BP_TYPE);
            if (type.equals("balance")) {
                mMissions.add(new BalanceMission(missionJSON));
            } else if (type.equals("record")) {
                mMissions.add(new RecordMission(missionJSON));
            } else if (type.equals("challenge")) {
                mMissions.add(new Challenge(missionJSON));
            } else {
                StoreUtils.LogError(TAG, "Unknown mission type: " + type);
            }
        }

        if (!isCompleted()) {
            BusProvider.getInstance().register(this);
        }
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        try {
            JSONArray missionsArr = new JSONArray();
            for (Mission mission : mMissions) {
                missionsArr.put(mission.toJSONObject());
            }
            jsonObject.put(BPJSONConsts.BP_MISSIONS, missionsArr);
            jsonObject.put(BPJSONConsts.BP_TYPE, "challenge");
        } catch (JSONException e) {
            StoreUtils.LogError(TAG, "An error occurred while generating JSON object.");
        }

        return jsonObject;
    }

    @Override
    public boolean isCompleted() {
        for (Mission mission : mMissions) {
            if (!mission.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    @Subscribe
    public void onMissionCompleted(MissionCompletedEvent missionCompletedEvent) {
        if (mMissions.contains(missionCompletedEvent.getMission())) {
            boolean completed = true;
            for (Mission mission : mMissions) {
                if (!mission.isCompleted()) {
                    completed = false;
                    break;
                }
            }

            if (completed) {
                BusProvider.getInstance().unregister(this);
                setCompleted(true);
            }
        }
    }
}
