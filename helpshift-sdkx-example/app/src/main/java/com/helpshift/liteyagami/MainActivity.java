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
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.helpshift.liteyagami.util.NotificationUtils;
import com.helpshift.liteyagami.util.StringUtils;
import com.helpshift.util.ApplicationUtil;
import com.helpshift.util.JsonUtils;
import com.helpshift.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
  private EditText genericConfigKeyEditText;
  private EditText genericConfigValueEditText;
  private CheckBox initiateChatOnLoad;
  private CheckBox showToastLogs;
  private EditText firstUserMessageEditText;
  private EditText conversationPrefillTextEditText;
  private EditText cifNameEditText;
  private EditText cifValueEditText;
  private Spinner cifTypeSpinner;

  private HashMap<String, Object> genericConfig = new HashMap<>();
  private Map<String, Object> cifs = new HashMap<>();

  private InternalFeatures internalFeatures;

  private final TextWatcher textWatcher = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
      updateConfigOnUI();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    storage = MainApplication.getAppStorage();

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
        if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
          Toast.makeText(MainActivity.this, eventName + " " + data.toString(), Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "eventName " + eventName + " " + Utils.prettyFormatHashMap(data, 0));

        if (HelpshiftEvent.RECEIVED_UNREAD_MESSAGE_COUNT.equals(eventName)) {
          int count = (int) data.get(HelpshiftEvent.DATA_MESSAGE_COUNT);
          boolean fromCache = (boolean) data.get(HelpshiftEvent.DATA_MESSAGE_COUNT_FROM_CACHE);

          AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
          dialog.setMessage("Count : " + count + ", From Cache: " + fromCache);
          dialog.setCancelable(true);
          dialog.show();
        }

        if (HelpshiftEvent.ACTION_CLICKED.equals(eventName)) {
          String actionType = (String) data.get(HelpshiftEvent.DATA_ACTION_TYPE);
          String actionData = (String) data.get(HelpshiftEvent.DATA_ACTION);

          //This is for edge Case if event gets invoked without passing any data
          if (Utils.isEmpty(actionType) && Utils.isEmpty(actionData)) {
            Log.d(TAG, "Event Received for " + eventName + " with no data");
            return;
          }

          if (Utils.isEmpty(actionType) || Utils.isEmpty(actionData)) {
            Log.d(TAG, "Event Received for " + eventName + " with actionType or action Data as empty");
            return;
          }

          if (actionType.equals(HelpshiftEvent.DATA_ACTION_TYPE_LINK)) {
            Log.d(TAG, "Event Received for " + eventName + " action type " + actionType + " link " + actionData);
          } else if (actionType.equals(HelpshiftEvent.DATA_ACTION_TYPE_CALL)) {
            Log.d(TAG, "Event Received for " + eventName + " action type " + actionType + " number " + actionData);
          }
        }

        if (HelpshiftEvent.AGENT_MESSAGE_RECEIVED.equals(eventName)) {
          handleAgentMessageReceivedEvent(data, eventName);
        }
      }

      @Override
      public void onUserAuthenticationFailure(HelpshiftAuthenticationFailureReason reason) {
          if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
              Toast.makeText(MainActivity.this,
                      "Error in user authentication: " + reason.name(), Toast.LENGTH_SHORT).show();
          }
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
    findViewById(R.id.genericConfigAdd).setOnClickListener(this);
    findViewById(R.id.genericConfigReset).setOnClickListener(this);
    findViewById(R.id.showCloseSupportNotification).setOnClickListener(this);
    findViewById(R.id.addCif).setOnClickListener(this);

    breadcrumbEditText = findViewById(R.id.breadCrumbEditText);

    logLevelDropDown = findViewById(R.id.logLevelSelection);
    logTagEditText = findViewById(R.id.logTagText);
    logMessageEditText = findViewById(R.id.logMessageText);
    genericConfigKeyEditText = findViewById(R.id.genericConfigKey);
    genericConfigValueEditText = findViewById(R.id.genericConfigValue);

    issueTagsEditText = findViewById(R.id.issueTags);
    issueTagsEditText.setText(storage.storageGet(StorageConstants.KEY_ISSUE_TAGS));
    issueTagsEditText.addTextChangedListener(textWatcher);

    firstUserMessageEditText = findViewById(R.id.firstUserMessage);
    firstUserMessageEditText.setText(storage.storageGet(StorageConstants.FIRST_USER_MESSAGE));
    firstUserMessageEditText.addTextChangedListener(textWatcher);

    conversationPrefillTextEditText = findViewById(R.id.conversationPrefillText);
    conversationPrefillTextEditText.setText(storage.storageGet(StorageConstants.CONVERSATION_PREFILL_TEXT));
    conversationPrefillTextEditText.addTextChangedListener(textWatcher);

    fullPrivacyCheckBox = findViewById(R.id.fullPrivacyCheck);
    fullPrivacyCheckBox.setChecked(storage.storageGetBoolean(StorageConstants.KEY_ENABLE_FULL_PRIVACY));
    fullPrivacyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        storage.storageSet(StorageConstants.KEY_ENABLE_FULL_PRIVACY, isChecked);
        updateConfigOnUI();
      }
    });

    initiateChatOnLoad = findViewById(R.id.initiateChatOnLoad);
    initiateChatOnLoad.setChecked(storage.storageGetBoolean(StorageConstants.INITIATE_CHAT_ON_LOAD,
            false));
    initiateChatOnLoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        storage.storageSet(StorageConstants.INITIATE_CHAT_ON_LOAD, isChecked);
        updateConfigOnUI();
      }
    });

    showToastLogs = findViewById(R.id.showToastMessage);
    showToastLogs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        storage.storageSet(StorageConstants.SHOW_TOAST_MESSAGE, isChecked);
      }
    });
    showToastLogs.setChecked(storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE,
            true));

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

    TextView sdkVersionText = findViewById(R.id.sdkversion);
    sdkVersionText.setText(Helpshift.getSDKVersion());

    cifNameEditText = findViewById(R.id.cifName);
    cifValueEditText = findViewById(R.id.cifValueEdit);
    cifTypeSpinner = findViewById(R.id.cifTypeSelect);

    try {
      cifs = JsonUtils.toMap(new JSONObject(storage.storageGet(StorageConstants.KEY_CIFS, "{}")));

      JSONObject jsonObject = new JSONObject(storage.storageGet(StorageConstants.GENERIC_CONFIG));
      Iterator<String> keys = jsonObject.keys();

      while (keys.hasNext()) {
        String key = keys.next();
        Object value = jsonObject.get(key);
        genericConfig.put(key, value);
      }
    } catch (JSONException e) {
      Log.e(TAG, "An exception occurred while restoring config:", e);
    }
    updateConfigOnUI();
  }

  private void handleAgentMessageReceivedEvent(Map<String, Object> eventData, String eventName) {

    if (HelpshiftEvent.AGENT_MESSAGE_RECEIVED.equals(eventName)) {
      String publishId = (String) eventData.get(HelpshiftEvent.DATA_PUBLISH_ID);
      String type = (String) eventData.get(HelpshiftEvent.DATA_MESSAGE_TYPE);
      String body = (String) eventData.get(HelpshiftEvent.DATA_MESSAGE_BODY);
      Long createdTs = (Long) eventData.get(HelpshiftEvent.DATA_CREATED_TIME);

      if (Utils.isEmpty(publishId) && Utils.isEmpty(type) && Utils.isEmpty(body) && createdTs == null) {
        Log.d(TAG, eventName + " Received no data");
        return;
      }

      // app_review_request message type body will be empty
      if (Utils.isEmpty(publishId) || Utils.isEmpty(type) || (Utils.isEmpty(body) && !HelpshiftEvent.DATA_MESSAGE_TYPE_APP_REVIEW_REQUEST.equals(type)) || createdTs == null) {
        Log.d(TAG, eventName + " Received incomplete data");
        return;
      }

      StringBuilder eventLog = new StringBuilder(eventName);
      eventLog.append(" publishId : ").append(publishId).append(" type : ").append(type);
      eventLog.append(" body : ").append(body).append(" createdTs :").append(createdTs);

      List<Object> attachments = (List<Object>) eventData.get(HelpshiftEvent.DATA_ATTACHMENTS);

      if (Utils.isEmpty(attachments)) {
        eventLog.append(".\nNo attachments received in message");
      } else {

        for (int i = 0; i < attachments.size(); i++) {
          Map<String, Object> attachment = (Map<String, Object>) attachments.get(i);

          eventLog.append("\nattachment ").append(i + 1);

          String url = (String) attachment.get(HelpshiftEvent.DATA_URL);
          String contentType = (String) attachment.get(HelpshiftEvent.DATA_CONTENT_TYPE);
          String fileName = (String) attachment.get(HelpshiftEvent.DATA_FILE_NAME);
          Integer size = (Integer) attachment.get(HelpshiftEvent.DATA_SIZE);

          if (Utils.isEmpty(url) && Utils.isEmpty(fileName) && size == null) {
            eventLog.append(" Received no data for attachment ").append(i + 1);
            continue;
          }

          if (Utils.isEmpty(url) || Utils.isEmpty(fileName) || size == null) {
            eventLog.append(" Received incomplete data for attachment ").append(i + 1);
            continue;
          }

          eventLog.append(" url : ").append(url).append(" contentType : ").append(contentType)
                  .append(" fileName : ").append(fileName).append(" size : ").append(size);
        }
      }

      Log.d(TAG, eventLog.toString());
    }
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
    Map<String, Object> config = new HashMap<>();

    updateConfigOnUI();

    config.putAll(genericConfig);
    config.putAll(getExplicitConfig());

    if (!cifs.isEmpty()) {
      config.put("cifs", cifs);
    } else {
      config.put("cifs", SampleAppConfig.defaultCIFs());
    }

    String faqSectiondId = sectionIdEditText.getText().toString();
    storage.storageSet(StorageConstants.SECTION_ID, faqSectiondId);

    String faqId = faqIdEditText.getText().toString();
    storage.storageSet(StorageConstants.FAQ_ID, faqId);

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
      case R.id.genericConfigAdd:
        handleAddGenericConfig();
        updateConfigOnUI();
        break;
      case R.id.genericConfigReset:
        handleResetConfig();
        updateConfigOnUI();
        break;
      case R.id.showCloseSupportNotification:
        showCloseSupportSessionNotification(MainApplication.getApplication());
        break;
      case R.id.addCif:
        handleCIFAddition();
        break;
    }

    internalFeatures.handleClicks(v.getId());
  }

  private void showCloseSupportSessionNotification(Context context) {

    if (context == null) {
      return;
    }

    Intent intent = new Intent(context, SampleNotificationActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(SampleNotificationActivity.EXTRA_CLOSE_SUPPORT_SESSION, true);

    NotificationUtils.showNotification(context, intent, SampleAppConfig.CHANNEL_ID,
            NotificationUtils.SESSION_CLOSE_NOTIFICATION_ID, "Close HelpShift Session",
            "Click to close helpshift session", R.drawable.hs__chat_icon, true);
  }

  private void handleCIFAddition() {
    String cifType = cifTypeSpinner.getSelectedItem().toString();
    if ("Type...".equalsIgnoreCase(cifType)) {
      if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
        Toast.makeText(this, "Select valid CIF type", Toast.LENGTH_SHORT).show();
      }
      return;
    }

    String cifName = cifNameEditText.getText().toString();
    Object cifValue = cifValueEditText.getText().toString();

    try {
      if ("date".equals(cifType)) {
        cifValue = Long.parseLong((String) cifValue);
      }
    } catch (Exception e) {
      if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
        Toast.makeText(this, "Invalid value for date type CIF", Toast.LENGTH_SHORT).show();
      }
      return;
    }


    Map<String, Object> cifItem = new HashMap<>();
    cifItem.put("type", cifType);
    cifItem.put("value", cifValue);

    cifs.put(cifName, cifItem);
    persistCIFMap(cifs);

    cifValueEditText.setText("");
    cifNameEditText.setText("");
    cifTypeSpinner.setSelection(0);

    updateConfigOnUI();
  }

  private Map<String,Object> getExplicitConfig(){
    Map<String, Object> explicitConfig = new HashMap<>();

    boolean fullPrivacy = fullPrivacyCheckBox.isChecked();
    boolean initiateChatOnLoading = initiateChatOnLoad.isChecked();

    String commaSeparatedTags = issueTagsEditText.getText().toString();
    storage.storageSet(StorageConstants.KEY_ISSUE_TAGS, commaSeparatedTags);

    String firstUserMessage = firstUserMessageEditText.getText().toString();
    storage.storageSet(StorageConstants.FIRST_USER_MESSAGE, firstUserMessage);

    String conversationPrefillText = conversationPrefillTextEditText.getText().toString();
    storage.storageSet(StorageConstants.CONVERSATION_PREFILL_TEXT, conversationPrefillText);

    explicitConfig.put("initiateChatOnLoad", initiateChatOnLoading);
    explicitConfig.put("fullPrivacy", fullPrivacy);

    if (!Utils.isEmpty(commaSeparatedTags)) {
      explicitConfig.put("tags", getTagsForConfig(commaSeparatedTags));
    } else {
      explicitConfig.remove("tags");
    }

    explicitConfig.put("initialUserMessage", firstUserMessage);
    explicitConfig.put("conversationPrefillText", conversationPrefillText);

    return explicitConfig;
  }

  private void resetExplicitConfigOnUI() {
    fullPrivacyCheckBox.setChecked(false);
    initiateChatOnLoad.setChecked(false);

    issueTagsEditText.setText("");
    storage.storageSet(StorageConstants.KEY_ISSUE_TAGS, "");

    firstUserMessageEditText.setText("");
    storage.storageSet(StorageConstants.FIRST_USER_MESSAGE, "");

    conversationPrefillTextEditText.setText("");
    storage.storageSet(StorageConstants.CONVERSATION_PREFILL_TEXT, "");
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

  private void handleAddGenericConfig(){
    String genericConfigKey = genericConfigKeyEditText.getText().toString();
    String genericConfigValue = genericConfigValueEditText.getText().toString();

    if(TextUtils.isEmpty(genericConfigKey) || TextUtils.isEmpty(genericConfigValue)) {
      if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
        Toast.makeText(this, "Key or value is empty!", Toast.LENGTH_SHORT).show();
      }
      return;
    }

    if (genericConfigKey.equals("tags")) {
      String[] configTags = getTagsForConfig(genericConfigValue);
      genericConfig.put(genericConfigKey, configTags);
    } else if ("true".equalsIgnoreCase(genericConfigValue) || "false".equalsIgnoreCase(genericConfigValue)) {
      genericConfig.put(genericConfigKey, Boolean.parseBoolean(genericConfigValue));
    } else {

      try {
        int value = Integer.parseInt(genericConfigValue);
        genericConfig.put(genericConfigKey, value);
      } catch (Exception e) {
        genericConfig.put(genericConfigKey, genericConfigValue);
      }
    }

    persistGenericConfig(genericConfig);

    genericConfigKeyEditText.setText("");
    genericConfigValueEditText.setText("");
  }

  private void handleResetConfig(){
    genericConfig.clear();
    persistGenericConfig(genericConfig);

    cifs.clear();
    persistCIFMap(cifs);

    resetExplicitConfigOnUI();
    if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
      Toast.makeText(this, "Custom config cleared", Toast.LENGTH_LONG).show();
    }
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
      if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
        Toast.makeText(this, "Permission already granted!", Toast.LENGTH_SHORT).show();
      }
    } else {
      // You can directly ask for the permission.
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
        requestPermissions(
                new String[]{"android.permission.POST_NOTIFICATIONS"},
                12);
      } else {
        if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
          Toast.makeText(this, "Not required!", Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == 12) {
      if (grantResults.length > 0 &&
              grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
          Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
        }
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

  private void updateConfigOnUI() {
    Map<String, Object> config = new HashMap<>();
    config.putAll(genericConfig);
    config.putAll(getExplicitConfig());
    config.put("cifs", cifs);

    TextView genericConfigText = findViewById(R.id.genericConfigJsonText);
    genericConfigText.setText(StringUtils.generatePrettyStringForMap(config, ""));
  }

  private String getConfigJsonString(Map<String, Object> config) {
    JSONObject jsonObject = new JSONObject(config);
    return jsonObject.toString();
  }

  private void persistGenericConfig(HashMap<String, Object> genericConfig){
    storage.storageSet(StorageConstants.GENERIC_CONFIG, getConfigJsonString(genericConfig));
  }

  private void persistCIFMap(Map<String, Object> cifs) {
    String json = "";
    if (cifs.isEmpty()) {
      json = "{}";
    } else {
      json = JsonUtils.mapToJsonString(cifs);
    }

    storage.storageSet(StorageConstants.KEY_CIFS, json);
  }
}
