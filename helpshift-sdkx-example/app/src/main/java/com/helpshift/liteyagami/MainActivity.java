package com.helpshift.liteyagami;

import static com.helpshift.liteyagami.config.SampleAppConfig.IS_INSTALL_CALL_DELAYED;
import static com.helpshift.liteyagami.config.SampleAppConfig.getInstallConfig;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.helpshift.HSDebugLog;
import com.helpshift.Helpshift;
import com.helpshift.HelpshiftAuthenticationFailureReason;
import com.helpshift.HelpshiftEvent;
import com.helpshift.HelpshiftEventsListener;
import com.helpshift.UnsupportedOSVersionException;
import com.helpshift.core.HSContext;
import com.helpshift.liteyagami.config.SampleAppConfig;
import com.helpshift.liteyagami.external.*;

import com.helpshift.liteyagami.storage.AppStorage;
import com.helpshift.liteyagami.storage.StorageConstants;
import com.helpshift.liteyagami.user.LoginActivity;
import com.helpshift.util.ApplicationUtil;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String TAG = "Helpshift_Demo";

  private AppStorage storage;
  private EditText issueTagsEditText;
  private CheckBox fullPrivacyCheckBox;
  private CheckBox fetchFromRemoteCheckBox;
  private EditText sectionIdEditText;
  private EditText faqIdEditText;
  private EditText languageEditText;
  private Spinner languageDropDown;
  private String[] languageCodes;
  private boolean shouldIgnoreFirstSelectCall = true;
  private EditText breadcrumbEditText;
  private Spinner logLevelDropDown;
  private EditText logTagEditText;
  private EditText logMessageEditText;
  private CheckBox enableInAppNotifications;

  private InternalFeatures internalFeatures;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    storage = new AppStorage(MainActivity.this);
    internalFeatures = new InternalFeaturesDummy();

    if (IS_INSTALL_CALL_DELAYED) {
      try {
        Helpshift.install(getApplication(),
                          SampleAppConfig.PLATFORM_ID,
                          SampleAppConfig.DOMAIN,
                          getInstallConfig());
      }
      catch (UnsupportedOSVersionException e) {
        Log.e(TAG, "install() called on the OS version: " + Build.VERSION.SDK_INT + " is not supported");
      }
    }

    internalFeatures.init();

    // Get FCM push token
    FirebaseInstanceId.getInstance().getInstanceId()
            .addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
      @Override
      public void onSuccess(InstanceIdResult instanceIdResult) {
        String newToken = instanceIdResult.getToken();
        storage.storageSet("pushToken", newToken);
      }
    });


    //creating notification channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createChannel();
    }

    Helpshift.setHelpshiftEventsListener(new HelpshiftEventsListener() {
      @Override
      public void onEventOccurred(@NonNull String eventName, Map<String, Object> data) {
        Toast.makeText(MainActivity.this, eventName + " " + data.toString(), Toast.LENGTH_SHORT).show();

        if (HelpshiftEvent.RECEIVED_UNREAD_MESSAGE_COUNT.equals(eventName)) {
          int count = (int) data.get(HelpshiftEvent.DATA_MESSAGE_COUNT);
          boolean fromCache = (boolean) data.get(HelpshiftEvent.DATA_MESSAGE_COUNT_FROM_CACHE);

          AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
          dialog.setMessage("Count : " + count + ", From Cache: " + fromCache);
          dialog.setCancelable(true);
          dialog.show();
        }
      }

      @Override
      public void onUserAuthenticationFailure(HelpshiftAuthenticationFailureReason reason) {
        Toast.makeText(MainActivity.this,
                "Error in user authentication: " + reason.name(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error in user authentication: " + reason.name());
      }
    });

    findViewById(R.id.open_helpshift).setOnClickListener(this);
    findViewById(R.id.login).setOnClickListener(this);
    findViewById(R.id.logout).setOnClickListener(this);
    findViewById(R.id.show_helpcenter).setOnClickListener(this);
    findViewById(R.id.show_faq_section).setOnClickListener(this);
    findViewById(R.id.show_single_faq).setOnClickListener(this);
    findViewById(R.id.unreadCount).setOnClickListener(this);
    findViewById(R.id.notifPermission).setOnClickListener(this);
    findViewById(R.id.setLanguage).setOnClickListener(this);
    findViewById(R.id.setBreadcrumb).setOnClickListener(this);
    findViewById(R.id.clearBreadcrumb).setOnClickListener(this);
    findViewById(R.id.addDebugLog).setOnClickListener(this);

    breadcrumbEditText = findViewById(R.id.breadCrumbEditText);

    logLevelDropDown = findViewById(R.id.logLevelSelection);
    logTagEditText = findViewById(R.id.logTagText);
    logMessageEditText = findViewById(R.id.logMessageText);

    issueTagsEditText = findViewById(R.id.issueTags);
    issueTagsEditText.setText(storage.storageGet(StorageConstants.KEY_ISSUE_TAGS));

    fullPrivacyCheckBox = findViewById(R.id.fullPrivacyCheck);
    fullPrivacyCheckBox.setChecked(storage.storageGetBoolean(StorageConstants.KEY_ENABLE_FULL_PRIVACY));
    fullPrivacyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        storage.storageSet(StorageConstants.KEY_ENABLE_FULL_PRIVACY, isChecked);
      }
    });

    enableInAppNotifications = findViewById(R.id.enableInAppNotifications);
    enableInAppNotifications.setChecked(storage.storageGetBoolean(StorageConstants.KEY_ENABLE_IN_APP_NOTIFICATION,
            true));
    enableInAppNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        storage.storageSet(StorageConstants.KEY_ENABLE_IN_APP_NOTIFICATION, isChecked);
      }
    });

    fetchFromRemoteCheckBox = findViewById(R.id.fromRemote);
    fetchFromRemoteCheckBox.setChecked(storage.storageGetBoolean(StorageConstants.KEY_FROM_REMOTE));
    fetchFromRemoteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        storage.storageSet(StorageConstants.KEY_FROM_REMOTE, isChecked);
      }
    });

    sectionIdEditText = findViewById(R.id.sectionIdText);
    sectionIdEditText.setText(storage.storageGet(StorageConstants.SECTION_ID));

    faqIdEditText = findViewById(R.id.faqIdText);
    faqIdEditText.setText(storage.storageGet(StorageConstants.FAQ_ID));

    String currentSetLang = storage.storageGet(StorageConstants.KEY_LANGUAGE);
    languageEditText = findViewById(R.id.languageEditText);
    languageEditText.setText(currentSetLang);

    setupLanguageSelectionUI(currentSetLang);

  }

  private void setupLanguageSelectionUI(String currentSetLang) {
    int languagePosition = -1;
    languageCodes = getResources().getStringArray(R.array.language_values);
    for (int i = 0; i < languageCodes.length; i++) {
      String lang = languageCodes[i];
      if (lang.equalsIgnoreCase(currentSetLang)) {
        languagePosition = i;
        break;
      }
    }

    languageDropDown = findViewById(R.id.languageDropDown);
    if (languagePosition != -1) {
      languageDropDown.setSelection(languagePosition);
    }

    languageDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Ignore first call that defaults to position 0 since we need to set the initial value ourselves.
        if (shouldIgnoreFirstSelectCall) {
          shouldIgnoreFirstSelectCall = false;
          return;
        }

        languageEditText.setText(languageCodes[position]);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        Log.i(TAG, "Nothing selected from language dropdown");
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    TextView appLevelLang = findViewById(R.id.appLevelLanguage);
    appLevelLang.setText(HSContext.getInstance().getDevice().getLanguage());
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (IS_INSTALL_CALL_DELAYED) {
      Helpshift.onAppForeground();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (IS_INSTALL_CALL_DELAYED) {
      Helpshift.onAppBackground();
    }
  }

  @Override
  public void onClick(View v) {
    Map<String, Object> config = SampleAppConfig.getSDKConfig();

    boolean enableInAppNotification = enableInAppNotifications.isChecked();
    boolean fullPrivacy = fullPrivacyCheckBox.isChecked();

    String commaSeparatedTags = issueTagsEditText.getText().toString();
    storage.storageSet(StorageConstants.KEY_ISSUE_TAGS, commaSeparatedTags);

    String faqSectiondId = sectionIdEditText.getText().toString();
    storage.storageSet(StorageConstants.SECTION_ID, faqSectiondId);

    String faqId = faqIdEditText.getText().toString();
    storage.storageSet(StorageConstants.FAQ_ID, faqId);

    config.put("enableInAppNotification", enableInAppNotification);
    config.put("fullPrivacy", fullPrivacy);
    config.put("tags", getTagsForConfig(commaSeparatedTags));


    switch (v.getId()) {
      case R.id.login:
        startLoginActivity();
        break;
      case R.id.open_helpshift:
        Helpshift.showConversation(MainActivity.this, config);
        break;
      case R.id.logout:
        Helpshift.logout();
        break;
      case R.id.show_helpcenter:
        Helpshift.showFAQs(MainActivity.this, config);
        break;
      case R.id.show_faq_section:
        Helpshift.showFAQSection(MainActivity.this, faqSectiondId, config);
        break;
      case R.id.show_single_faq:
        Helpshift.showSingleFAQ(MainActivity.this, faqId, config);
        break;
      case R.id.unreadCount:
        Helpshift.requestUnreadMessageCount(fetchFromRemoteCheckBox.isChecked());
        break;
      case R.id.notifPermission:
        handleNotificationPermissionClick();
        break;
      case R.id.setLanguage:
        handleSDKLanguageSet(languageEditText.getText().toString());
        break;
      case R.id.setBreadcrumb:
        handleLeaveBreadcrumb(breadcrumbEditText.getText().toString());
        break;
      case R.id.clearBreadcrumb:
        handleClearBreadcrumb();
        break;
      case R.id.addDebugLog:
        handleAddDebugLog();
        break;
    }

    internalFeatures.handleClicks(v.getId());
  }

  private void handleAddDebugLog() {
    String level = (String) logLevelDropDown.getSelectedItem();
    String tag = logTagEditText.getText().toString();
    String message = logMessageEditText.getText().toString();

    switch (level) {
      case "Verbose":
        HSDebugLog.v(tag, message);
        break;
      case "Debug":
        HSDebugLog.d(tag, message);
        break;
      case "Info":
        HSDebugLog.i(tag, message);
        break;
      case "Warn":
        HSDebugLog.w(tag, message);
        break;
      case "Error":
        HSDebugLog.e(tag, message);
        break;
    }

    logMessageEditText.setText("");
    logTagEditText.setText("");
  }

  private void handleLeaveBreadcrumb(String crumb) {
    Helpshift.leaveBreadCrumb(crumb);
    breadcrumbEditText.setText("");
  }

  private void handleClearBreadcrumb() {
    Helpshift.clearBreadCrumbs();
  }

  private void handleSDKLanguageSet(String languageCode) {
    storage.storageSet(StorageConstants.KEY_LANGUAGE, languageCode);
    Helpshift.setLanguage(languageCode);
  }


  private void handleNotificationPermissionClick() {
    if (ContextCompat.checkSelfPermission(
            this, "android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this, "Permission already granted!", Toast.LENGTH_SHORT).show();
    } else {
      // You can directly ask for the permission.
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
        requestPermissions(
                new String[]{"android.permission.POST_NOTIFICATIONS"},
                12);
      } else {
        Toast.makeText(this, "Not required!", Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == 12) {
      if (grantResults.length > 0 &&
              grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private String[] getTagsForConfig(String tags){
    String[] tagsArray = tags.split(",");
    return tagsArray;
  }

  private void startLoginActivity() {
    Intent loginIntent = new Intent(this, LoginActivity.class);
    startActivity(loginIntent);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void createChannel() {
    String id = SampleAppConfig.CHANNEL_ID;
    NotificationManager notificationManager = ApplicationUtil.getNotificationManager(this);
    if (notificationManager != null) {
      NotificationChannel notificationChannel = notificationManager.getNotificationChannel(id);
      //Notification channel not exist so create new one
      if (notificationChannel == null) {
        String name = SampleAppConfig.CHANNEL_ID;
        String description = "TESTING CHANNEL";
        //Create the channel with default ID
        NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
        mChannel.setDescription(description);

        Uri soundUri = getNotificationSoundUri(this, R.raw.custom_notification);
        if (soundUri != null) {
          mChannel.setSound(soundUri, new AudioAttributes.Builder().build());
        }
        notificationManager.createNotificationChannel(mChannel);
      }
    }
  }

  public static Uri getNotificationSoundUri(Context context, int notificationSoundId) {
    Uri soundUri = null;
    if (notificationSoundId != 0) {
      String soundUriString = "android.resource://" + context.getPackageName() + "/" + notificationSoundId;
      soundUri = Uri.parse(soundUriString);
    }
    return soundUri;
  }
}
