package com.helpshift.liteyagami;

import android.app.Application;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.helpshift.Helpshift;
import com.helpshift.UnsupportedOSVersionException;
import com.helpshift.liteyagami.config.SampleAppConfig;
import com.helpshift.liteyagami.proactive.ProactiveLocalAPIConfigProvider;
import com.helpshift.liteyagami.storage.AppStorage;

import static com.helpshift.liteyagami.config.SampleAppConfig.getInstallConfig;

public class MainApplication extends Application {

  private static MainApplication instance;
  public static AppStorage storage;

  @Override
  public void onCreate() {
    super.onCreate();

    instance = this;
    storage = new AppStorage(this);

    String domain = storage.storageGet(SampleAppConfig.DOMAIN_KEY);
    String platformId = storage.storageGet(SampleAppConfig.PLATFORM_ID_KEY);

    if (TextUtils.isEmpty(domain) || TextUtils.isEmpty(platformId)) {
      domain = SampleAppConfig.DOMAIN.trim();
      platformId = SampleAppConfig.PLATFORM_ID.trim();
    }
    // Install call
    if (!SampleAppConfig.IS_INSTALL_CALL_DELAYED) {
      try {
        Helpshift.install(this,
                          platformId,
                          domain,
                          getInstallConfig());
      }
      catch (UnsupportedOSVersionException e) {
        Log.e("MainApp", "install() called on the OS version: " + Build.VERSION.SDK_INT + " is not supported");
      }
    }

    // Set listener to collect local config when handling Proactive Outbound support links.
    Helpshift.setHelpshiftProactiveConfigCollector(new ProactiveLocalAPIConfigProvider());

  }

  public static Application getApplication(){
    return instance;
  }

  public static AppStorage getAppStorage() {
    return storage;
  }
}
