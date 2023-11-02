package com.helpshift.liteyagami.proactive;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.helpshift.Helpshift;

/**
 * Class to open webchat/Helpcenter from notification based on action in proactive config payload
 **/

public class ProactiveNotificationActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.getBoolean("proactiveNotification")) {
            String url = bundle.getString("proactiveLink");
            Helpshift.handleProactiveLink(url);
        }
        finish();
    }
}
