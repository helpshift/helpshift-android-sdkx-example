package com.helpshift.liteyagami.config;


import com.helpshift.liteyagami.BuildConfig;
import com.helpshift.liteyagami.R;
import com.helpshift.util.ConfigValues;
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

  public static Map<String, Object> getSDKConfig(){
    Map<String, Object> config = new HashMap<>();
    config.put("customIssueFields", getCifs());
    return config;
  }

  public static Map<String, Object> getInstallConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("enableLogging", true);
    config.put("notificationSoundId", R.raw.custom_notification);
    config.put("notificationIcon", R.drawable.hs__chat_icon);
    config.put("notificationChannelId", SampleAppConfig.CHANNEL_ID);
    config.put("notificationLargeIcon", R.drawable.airplane);

    // Uncomment to use lock screen orientation config
    //config.put("screenOrientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    if (IS_INSTALL_CALL_DELAYED) {
      config.put(ConfigValues.MANUAL_LIFECYCLE_TRACKING, true);
    }
    return config;
  }

  public static Map<String, Object> getCifs() {
    Map<String, String> joiningDate = new HashMap<>();
    joiningDate.put("type", "dt");
    joiningDate.put("value", "1505927361535");

    Map<String, String> stockLevel = new HashMap<>();
    stockLevel.put("type", "n");
    stockLevel.put("value", "1505");

    Map<String, String> employeeName = new HashMap<>();
    employeeName.put("type", "sl");
    employeeName.put("value", "Bugs helpshift");

    Map<String, String> isPro = new HashMap<>();
    isPro.put("type", "b");
    isPro.put("value", "true");

    Map<String, Object> cifMap = new HashMap<>();
    cifMap.put("joining_date", joiningDate);
    cifMap.put("stock_level", stockLevel);
    cifMap.put("employee_name", employeeName);
    cifMap.put("is_pro", isPro);
    return cifMap;
  }
}
