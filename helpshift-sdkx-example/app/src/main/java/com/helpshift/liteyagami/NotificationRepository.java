package com.helpshift.liteyagami;

import com.helpshift.liteyagami.storage.AppStorage;
import com.helpshift.liteyagami.storage.StorageConstants;
import com.helpshift.log.HSLogger;
import com.helpshift.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class NotificationRepository {

    private static final String TAG = "NotificationRepository";
    private final AppStorage appStorage;

    public NotificationRepository(AppStorage appStorage) {
        this.appStorage = appStorage;
    }

    public void addPayload(JSONObject jsonObj) {
        String existing = appStorage.storageGet(StorageConstants.PUSH_PAYLOAD_KEY);
        JSONArray jsonArray;

        try {
            jsonArray = Utils.isEmpty(existing) ? new JSONArray() : new JSONArray(existing);

            jsonArray.put(jsonObj);
            appStorage.storageSet(
                    StorageConstants.PUSH_PAYLOAD_KEY,
                    jsonArray.toString());

        } catch (JSONException e) {
            HSLogger.e(TAG, "Error in adding payload to storage", e);
        }
    }
}
