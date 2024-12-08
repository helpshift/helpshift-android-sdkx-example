package com.helpshift.liteyagami;

import static com.helpshift.liteyagami.config.SampleAppConfig.getInstallConfig;

import android.app.Application;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.helpshift.Helpshift;
import com.helpshift.UnsupportedOSVersionException;
import com.helpshift.liteyagami.config.SampleAppConfig;
import com.helpshift.liteyagami.eventlistener.EventsListener;
import com.helpshift.liteyagami.eventlistener.HelpshiftEventsFlow;
import com.helpshift.liteyagami.proactive.ProactiveLocalAPIConfigProvider;

public class MainApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    InstanceProvider.initInstance(this);

    String domain = InstanceProvider.getInstance().getAppStorage().storageGet(SampleAppConfig.DOMAIN_KEY);
    String platformId = InstanceProvider.getInstance().getAppStorage().storageGet(SampleAppConfig.PLATFORM_ID_KEY);

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
    HelpshiftEventsFlow.initInstance();
    Helpshift.setHelpshiftEventsListener(new EventsListener());

    // Set listener to collect local config when handling Proactive Outbound support links.
    Helpshift.setHelpshiftProactiveConfigCollector(new ProactiveLocalAPIConfigProvider());
  }
}
