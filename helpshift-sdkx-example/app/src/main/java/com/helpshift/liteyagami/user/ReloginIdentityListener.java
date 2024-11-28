package com.helpshift.liteyagami.user;

import android.util.Log;

import com.helpshift.HelpshiftUserLoginEventsListener;

import java.util.Map;

public class ReloginIdentityListener implements HelpshiftUserLoginEventsListener {

  private static final String TAG = "ReLoginListener";

  @Override
  public void onLoginSuccess() {
    Log.d(TAG, "Re-login success");
  }

  @Override
  public void onLoginFailure(String s, Map<String, String> map) {
    Log.d(TAG, "Relogin failed: " + s + ", " + map);
  }
}
