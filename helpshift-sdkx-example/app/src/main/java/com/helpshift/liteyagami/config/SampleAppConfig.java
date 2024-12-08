package com.helpshift.liteyagami.config;


import android.util.Log;

import com.helpshift.liteyagami.BuildConfig;
import com.helpshift.liteyagami.InstanceProvider;
import com.helpshift.liteyagami.R;
import com.helpshift.liteyagami.storage.AppStorage;
import com.helpshift.liteyagami.storage.StorageConstants;
import com.helpshift.util.ConfigValues;
import com.helpshift.util.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SampleAppConfig {

  // pointed this to production - Gayatri domain - Lite SDK production App
  public static final String DOMAIN = BuildConfig.DOMAIN;
  public static final String PLATFORM_ID = BuildConfig.PLATFORM_ID;
  public static final String CHANNEL_ID = "LITE_SDK_CHANNEL";

  // set this flag to true if want to test the delayed install call
  public static boolean IS_INSTALL_CALL_DELAYED = false;
  public static final String DOMAIN_KEY = "domain";
  public static final String PLATFORM_ID_KEY = "platformId";

  private SampleAppConfig() {
    // empty
  }

  public static Map<String, Object> getStoredCIFAsConfig() {

    Map<String, Object> config = new HashMap<>();
    try {
      AppStorage storage = InstanceProvider.getInstance().getAppStorage();
      config.put("cifs", JsonUtils.toMap(new JSONObject(storage.storageGet(StorageConstants.KEY_CIFS, "{}"))));
    } catch (JSONException e) {
      Log.e("Helpshift", "Error reading stored CIFs", e);
    }
    return config;
  }

  public static Map<String, Object> getInstallConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("enableLogging", true);
    config.put("notificationSoundId", R.raw.custom_notification);
    config.put("notificationIcon", com.helpshift.R.drawable.hs__chat_icon);
    config.put("notificationChannelId", SampleAppConfig.CHANNEL_ID);
    config.put("notificationLargeIcon", R.drawable.airplane);

    // Uncomment to use lock screen orientation config
    //config.put("screenOrientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    if (IS_INSTALL_CALL_DELAYED) {
      config.put(ConfigValues.MANUAL_LIFECYCLE_TRACKING, true);
    }
    return config;
  }

  public static Map<String, Object> defaultCIFs() {
    Map<String, Object> cifMap = new HashMap<>();

    cifMap.put("joining_date", createMap("date", 1505927361535L));
    cifMap.put("stock_level", createMap("number", "1505"));
    cifMap.put("employee_name", createMap("singleline", "Bugs helpshift"));
    cifMap.put("is_pro", createMap("checkbox", "true"));

    return cifMap;
  }

  private static Map<String, Object> createMap(String type, Object value) {
    Map<String, Object> map = new HashMap<>();
    map.put("type", type);
    map.put("value", value);
    return map;
  }

}
