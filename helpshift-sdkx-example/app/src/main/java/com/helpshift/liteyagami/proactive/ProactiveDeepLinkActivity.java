package com.helpshift.liteyagami.proactive;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.helpshift.Helpshift;

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

        if (scheme.equals("myscheme") && host.equals("helpshift.com") && "proactive".equals(firstPath)) {
            Helpshift.handleProactiveLink(data.toString());
            finish();
        } else {
            Toast.makeText(this, "Invalid link to handle", Toast.LENGTH_SHORT).show();
        }
    }

}
