package com.helpshift.liteyagami.firebase;

import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.helpshift.Helpshift;
import com.helpshift.liteyagami.InstanceProvider;
import com.helpshift.liteyagami.NotificationRepository;
import com.helpshift.liteyagami.storage.AppStorage;
import com.helpshift.liteyagami.storage.StorageConstants;
import com.helpshift.log.HSLogger;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "HelpshiftSDK_Push";
    NotificationRepository repository;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        repository = InstanceProvider.getInstance().getNotificationRepository();

        // Handling push notification from Helpshift.
        // This handling is for notifications sent from Helpshift when agent replies to an issue from Helpshift dashboard.
        String origin = data.get("origin");
        if (origin != null && origin.equals("helpshift")) {
            Helpshift.handlePush(data);
        }

        //Adding current time in copy of data to show Push Notifications
        //Note : data is modified by adding time
        HSLogger.d(TAG, "Payload from push notification. \n" + data);
        addTime(data);
        repository.addPayload(new JSONObject(data));
    }

    @Override
    public void onNewToken(String newToken) {
        HSLogger.d(TAG, "Push token received: " + newToken);

        AppStorage appStorage = InstanceProvider.getInstance().getAppStorage();
        String pushToken = appStorage.storageGet(StorageConstants.PUSH_TOKEN, "");

        if (TextUtils.isEmpty(pushToken) || !pushToken.equals(newToken)) {
            appStorage.storageSet(StorageConstants.PUSH_TOKEN, newToken);
            Helpshift.registerPushToken(newToken);
        }
    }


    void addTime(Map<String, String> obj) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        obj.put("time", sdf.format(new Date()));
    }
}
