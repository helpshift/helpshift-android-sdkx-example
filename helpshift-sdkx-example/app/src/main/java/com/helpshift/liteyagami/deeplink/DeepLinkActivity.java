package com.helpshift.liteyagami.deeplink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.helpshift.Helpshift;
import com.helpshift.liteyagami.InstanceProvider;
import com.helpshift.liteyagami.config.SampleAppConfig;
import com.helpshift.liteyagami.storage.StorageConstants;

import java.util.Map;

public class DeepLinkActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    Uri data = intent.getData();

    String scheme = data.getScheme();
    String host = data.getHost();

    Map<String, Object> configMap = SampleAppConfig.getStoredCIFAsConfig();

    // In this example, we handle the following URL pattern via deeplinks
    // myscheme://example.com/?sectionid=489
    // myscheme://example.com/?faqid=12
    if (scheme.equals("myscheme") && host.equals("example.com")) {
      if (data.getQueryParameter("faqid") != null) {
        Helpshift.showSingleFAQ(this, data.getQueryParameter("faqid"), configMap);
      } else if (data.getQueryParameter("sectionid") != null) {
        Helpshift.showFAQSection(this, data.getQueryParameter("sectionid"), configMap);
      } else {
        Helpshift.showFAQs(this, configMap);
      }
      finish();
    } else {
      if (InstanceProvider.getInstance().getAppStorage().storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
        Toast.makeText(this, "Invalid link to handle", Toast.LENGTH_SHORT).show();
      }
    }
  }
}