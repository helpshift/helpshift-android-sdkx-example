package com.helpshift.liteyagami.eventlistener;

import static com.helpshift.liteyagami.util.StorageConstants.JWT_SECRET_KEY;
import static com.helpshift.liteyagami.util.StorageConstants.LOGIN_DATA_KEY;
import static com.helpshift.liteyagami.util.StorageConstants.USER_IDENTITIES_KEY;

import android.util.Log;

import androidx.annotation.NonNull;

import com.helpshift.Helpshift;
import com.helpshift.HelpshiftAuthenticationFailureReason;
import com.helpshift.HelpshiftEvent;
import com.helpshift.HelpshiftEventsListener;
import com.helpshift.liteyagami.InstanceProvider;
import com.helpshift.liteyagami.mockUserJWTTokenServer.MockBackendUserJWTTokenServer;
import com.helpshift.liteyagami.storage.AppStorage;
import com.helpshift.liteyagami.user.ReloginIdentityListener;
import com.helpshift.util.JsonUtils;
import com.helpshift.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class EventsListener implements HelpshiftEventsListener {

    private static final String TAG = "DemoEventsListener";
    final HelpshiftEventsFlow helpshiftEventsFlow = HelpshiftEventsFlow.getInstance();

    @Override
    public void onEventOccurred(@NonNull String eventName, Map<String, Object> data) {
        HelpshiftEventData helpshiftEventData = new HelpshiftEventData(eventName, data);
        helpshiftEventsFlow.addHelpshiftEvent(helpshiftEventData);

        if (HelpshiftEvent.USER_SESSION_EXPIRED.equals(eventName)) {
            reloginUser();
        }
    }

    @Override
    public void onUserAuthenticationFailure(HelpshiftAuthenticationFailureReason helpshiftAuthenticationFailureReason) {
        Log.e("EventsListener", "Error in user authentication: " + helpshiftAuthenticationFailureReason.name());

        HelpshiftEventData helpshiftEventData = new HelpshiftEventData(
                helpshiftAuthenticationFailureReason.name(), new HashMap<>());
        helpshiftEventsFlow.addHelpshiftEvent(helpshiftEventData);
    }

    private void reloginUser() {
      Log.d(TAG, "Trying to relogin user");

      Utils.executeWithDelay(new Runnable() {
        @Override
        public void run() {
          AppStorage storage = InstanceProvider.getInstance().getAppStorage();
          String userIdentityJson = storage.storageGet(USER_IDENTITIES_KEY, "");
          String loginDataJson = storage.storageGet(LOGIN_DATA_KEY, "");

          Map<String, Object> userIdentities = JsonUtils.jsonStringToMap(userIdentityJson);
          Map<String, Object> loginConfig = JsonUtils.jsonStringToMap(loginDataJson);
          String secret = storage.storageGet(JWT_SECRET_KEY);

          MockBackendUserJWTTokenServer.initSecretKey(secret);
          try {
            // Add some delay to imitate server latency
            Thread.sleep(2000);
          }
          catch (InterruptedException e) {
            throw new RuntimeException(e);
          }

          // If empty identities then it will be an anonymous login
          if (Utils.isEmpty(userIdentities)) {
            Log.d(TAG, "Relogin: Anonymous user");
            Helpshift.loginWithIdentity("", loginConfig, new ReloginIdentityListener());
            return;
          }

          // Remove iat since last used may have expired.
          userIdentities.remove("iat");

          String latestUserIdentityJson = JsonUtils.mapToJsonString(userIdentities);
          String token = MockBackendUserJWTTokenServer.generateJWTForUser(latestUserIdentityJson);

          Log.d(TAG, "Relogin: JWT user");
          Helpshift.loginWithIdentity(token, loginConfig, new ReloginIdentityListener());
        }
      }, 1000);
    }
}
