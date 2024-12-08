package com.helpshift.liteyagami.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.helpshift.Helpshift;
import com.helpshift.liteyagami.config.SampleAppConfig;
import com.helpshift.liteyagami.util.NotificationUtils;
import com.helpshift.liteyagami.proactive.ProactiveNotificationActivity;
import com.helpshift.log.HSLogger;
import com.helpshift.util.Utils;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

  private static final String TAG = "HelpshiftSDK_Push";

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    Map<String, String> data = remoteMessage.getData();

    // Handling push notification from Helpshift.
    // This handling is for notifications sent from Helpshift when agent replies to an issue from Helpshift dashboard.
    String origin = data.get("origin");
    if (origin != null && origin.equals("helpshift")) {
      Helpshift.handlePush(data);
    }

    // Handle notifications sent from client app's backend when sending proactive outbound notifications.
    // This notification does not originate from Helpshift's backend.
    // Example:
    // Payload from push notification, i.e data, contains proactive url (generated from Helpshift dashboard)
    // in the key "helpshift_proactive_link"
    generateProactiveNotification(data);
  }

  @Override
  public void onNewToken(String newToken) {
    HSLogger.d(TAG, "Push token received: " + newToken);

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    String pushToken = sharedPreferences.getString("push_token", "");

    if (TextUtils.isEmpty(pushToken) || !pushToken.equals(newToken)) {
      sharedPreferences.edit().putString("push_token", newToken).apply();
      Helpshift.registerPushToken(newToken);
    }
  }

  private void generateProactiveNotification(Map<String, String> data) {
    String proactiveUrl = data.get("helpshift_proactive_link");

    if (Utils.isEmpty(proactiveUrl)) {
      Log.i(TAG, "Push notification does not contain Proactive Outbound url");
      return;
    }

    Context context = getApplicationContext();

    Intent intent = new Intent(context, ProactiveNotificationActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra("proactiveNotification", true);
    intent.putExtra("proactiveLink", proactiveUrl);

    NotificationUtils.showNotification(context,intent,SampleAppConfig.CHANNEL_ID, NotificationUtils.NOTIFICATION_ID,data.get("title"),data.get("message"), com.helpshift.R.drawable.hs__chat_icon,true);
  }
}
