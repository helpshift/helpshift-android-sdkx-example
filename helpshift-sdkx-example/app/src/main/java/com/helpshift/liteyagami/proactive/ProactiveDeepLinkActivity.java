package com.helpshift.liteyagami.proactive;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.helpshift.Helpshift;
import com.helpshift.liteyagami.MainApplication;
import com.helpshift.liteyagami.storage.StorageConstants;

import java.util.List;

/**
 * Example logic to handle Proactive Outbound link as deeplinks embedded in different channels.
 */
public class ProactiveDeepLinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();

        String scheme = data.getScheme();
        String host = data.getHost();

        // Check for "proactive" path when dealing with Proactive outbound links
        List<String> pathSegments = data.getPathSegments();
        String firstPath = pathSegments != null && pathSegments.size() > 0 ? pathSegments.get(0) : "";

        // In this example, we handle the following proactive outbound link pattern
        // myscheme://helpshift.com/proactive/?payload=eyJhY3Rpb24iOiJoYy1hcHAiLCJtZXRhIjp7ImlkIjoib3V0Ym91bmRfZGM2NDVhMzQtOTk3YS00ZTZlLThkMzktNDZiNDJjNTI3NTMyIiwib3JpZ2luIjoicHJvYWN0aXZlIn0sImNoYXRDb25maWciOnsidGFncyI6WyJwaG9uZWdhcCIsImFzc3VzIl19fQ%3D%3D
        if (scheme.equals("myscheme") && host.equals("helpshift.com") && "proactive".equals(firstPath)) {
            Helpshift.handleProactiveLink(data.toString());
            finish();
        } else {
            if (MainApplication.getAppStorage().storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
                Toast.makeText(this, "Invalid link to handle", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
