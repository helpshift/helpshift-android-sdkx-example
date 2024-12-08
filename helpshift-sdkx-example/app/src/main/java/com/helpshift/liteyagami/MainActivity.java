package com.helpshift.liteyagami;

import static com.helpshift.liteyagami.config.SampleAppConfig.IS_INSTALL_CALL_DELAYED;
import static com.helpshift.liteyagami.config.SampleAppConfig.getInstallConfig;
import static com.helpshift.liteyagami.util.StorageConstants.IDENTITY_TOKEN_KEY;
import static com.helpshift.liteyagami.util.StorageConstants.LOGIN_DATA_KEY;
import static com.helpshift.liteyagami.util.StorageConstants.USER_IDENTITIES_KEY;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.helpshift.HSDebugLog;
import com.helpshift.Helpshift;
import com.helpshift.HelpshiftEvent;
import com.helpshift.UnsupportedOSVersionException;
import com.helpshift.core.HSContext;
import com.helpshift.liteyagami.config.SampleAppConfig;
import com.helpshift.liteyagami.eventlistener.HSEventsFlowListener;
import com.helpshift.liteyagami.eventlistener.HelpshiftEventData;
import com.helpshift.liteyagami.eventlistener.HelpshiftEventsFlow;
import com.helpshift.liteyagami.storage.AppStorage;
import com.helpshift.liteyagami.storage.StorageConstants;
import com.helpshift.liteyagami.user.LoginActivity;
import com.helpshift.liteyagami.user.UserWithIdentityActivity;
import com.helpshift.liteyagami.util.NotificationUtils;
import com.helpshift.liteyagami.util.StringUtils;
import com.helpshift.log.HSLogger;
import com.helpshift.util.ApplicationUtil;
import com.helpshift.util.JsonUtils;
import com.helpshift.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, HSEventsFlowListener {

  private static final String TAG = "Helpshift_Demo";

  private AppStorage storage;
  private String[] languageCodes;
  private boolean shouldIgnoreFirstSelectCall = true;

  private EditText issueTagsEditText, sectionIdEditText, faqIdEditText, languageEditText,
          breadcrumbEditText, logTagEditText, logMessageEditText,
          genericConfigKeyEditText, genericConfigValueEditText,
          firstUserMessageEditText, conversationPrefillTextEditText,
          cifNameEditText, cifValueEditText;

  private CheckBox fullPrivacyCheckBox, fetchFromRemoteCheckBox, initiateChatOnLoad, clearAnonUserCheckbox, showToastLogs;

  private Spinner languageDropDown, logLevelDropDown, cifTypeSpinner;

  private HashMap<String, Object> genericConfig = new HashMap<>();
  private Map<String, Object> cifs = new HashMap<>();

  private final TextWatcher textWatcher = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      // Ignore
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      // Ignore
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

    storage = InstanceProvider.getInstance().getAppStorage();

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

    findViewById(R.id.open_helpshift).setOnClickListener(this);
    findViewById(R.id.login).setOnClickListener(this);
    findViewById(R.id.logout).setOnClickListener(this);
    findViewById(R.id.user_identity_button).setOnClickListener(this);
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
    findViewById(R.id.showHelpshiftEvents).setOnClickListener(this);
    findViewById(R.id.clearAnonUser).setOnClickListener(this);

    clearAnonUserCheckbox = findViewById(R.id.clearAnonUserCheckBox);
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

      String genericConfigData = storage.storageGet(StorageConstants.GENERIC_CONFIG);
      JSONObject jsonObject = new JSONObject(Utils.isEmpty(genericConfigData) ? "{}" : genericConfigData);
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

    if (!HelpshiftEvent.AGENT_MESSAGE_RECEIVED.equals(eventName)) {
      return;
    }

    String publishId = (String) eventData.get(HelpshiftEvent.DATA_PUBLISH_ID);
    String type = (String) eventData.get(HelpshiftEvent.DATA_MESSAGE_TYPE);
    String body = (String) eventData.get(HelpshiftEvent.DATA_MESSAGE_BODY);
    Long createdTs = (Long) eventData.get(HelpshiftEvent.DATA_CREATED_TIME);

    // Check if the main fields are empty or null
    if (isDataIncomplete(publishId, type, body, createdTs, eventName)) {
      return;
    }

    // Build the event log message
    StringBuilder eventLog = new StringBuilder(eventName)
            .append(" publishId : ").append(publishId)
            .append(" type : ").append(type)
            .append(" body : ").append(body)
            .append(" createdTs : ").append(createdTs);

    List<Object> attachments = (List<Object>) eventData.get(HelpshiftEvent.DATA_ATTACHMENTS);

    // Handle attachments, if any
    appendAttachmentsLog(attachments, eventLog);

    Log.d(TAG, eventLog.toString());
  }

  private boolean isDataIncomplete(String publishId, String type, String body, Long createdTs, String eventName) {
    // Check if all the main fields are empty
    if (Utils.isEmpty(publishId) && Utils.isEmpty(type) && Utils.isEmpty(body) && createdTs == null) {
      Log.d(TAG, eventName + " Received no data");
      return true;
    }

    // Special case: app_review_request type allows empty body
    if (Utils.isEmpty(publishId) || Utils.isEmpty(type) ||
            (Utils.isEmpty(body) && !HelpshiftEvent.DATA_MESSAGE_TYPE_APP_REVIEW_REQUEST.equals(type)) || createdTs == null) {
      Log.d(TAG, eventName + " Received incomplete data");
      return true;
    }

    return false;
  }

  private void appendAttachmentsLog(List<Object> attachments, StringBuilder eventLog) {
    if (Utils.isEmpty(attachments)) {
      eventLog.append(".\nNo attachments received in message");
      return;
    }

    for (int i = 0; i < attachments.size(); i++) {
      Map<String, Object> attachment = (Map<String, Object>) attachments.get(i);

      eventLog.append("\nattachment ").append(i + 1);

      String url = (String) attachment.get(HelpshiftEvent.DATA_URL);
      String contentType = (String) attachment.get(HelpshiftEvent.DATA_CONTENT_TYPE);
      String fileName = (String) attachment.get(HelpshiftEvent.DATA_FILE_NAME);
      Integer size = (Integer) attachment.get(HelpshiftEvent.DATA_SIZE);

      if (isAttachmentDataIncomplete(url, fileName, size, i, eventLog)) {
        continue;
      }

      eventLog.append(" url : ").append(url)
              .append(" contentType : ").append(contentType)
              .append(" fileName : ").append(fileName)
              .append(" size : ").append(size);
    }
  }

  private boolean isAttachmentDataIncomplete(String url, String fileName, Integer size, int index, StringBuilder eventLog) {
    // Check if the attachment fields are empty or null
    if (Utils.isEmpty(url) && Utils.isEmpty(fileName) && size == null) {
      eventLog.append(" Received no data for attachment ").append(index + 1);
      return true;
    }

    if (Utils.isEmpty(url) || Utils.isEmpty(fileName) || size == null) {
      eventLog.append(" Received incomplete data for attachment ").append(index + 1);
      return true;
    }

    return false;
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
    HelpshiftEventsFlow.setHelpshiftEventFlowListener(this);

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
        logoutUser();
        break;
      case R.id.user_identity_button:
        startUserIdentityActivity();
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
        showCloseSupportSessionNotification(InstanceProvider.getInstance().getMainApplication());
        break;
      case R.id.addCif:
        handleCIFAddition();
        break;
      case R.id.showHelpshiftEvents:
        showHelpshiftEvents();
        break;
      case R.id.clearAnonUser:
        Helpshift.clearAnonymousUserOnLogin(clearAnonUserCheckbox.isChecked());
        String toast = clearAnonUserCheckbox.isChecked() ? "Anonymous User cleared" : "Anonymous User will be retained";
        Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
        break;
      default:
        break;
    }
  }

  private void logoutUser() {
    Helpshift.logout();
    storage.storageSet(USER_IDENTITIES_KEY, "");
    storage.storageSet(IDENTITY_TOKEN_KEY, "");
    storage.storageSet(LOGIN_DATA_KEY, "");
  }

  private void showHelpshiftEvents() {
    try {
      List<HelpshiftEventData> helpshiftEventsList = HelpshiftEventsFlow.getInstance().getHelpshiftEvents();
      JSONArray helpshiftEventsFlowList = new JSONArray();

      for (HelpshiftEventData helpshiftEventData : helpshiftEventsList) {
        JSONObject event = new JSONObject();
        event.put("name", helpshiftEventData.getEventName());
        event.put("data", new JSONObject(helpshiftEventData.getData()));
        helpshiftEventsFlowList.put(event);
      }

      AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
              .setTitle("Helpshift Events")
              .setMessage(helpshiftEventsFlowList.toString(4))
              .setPositiveButton("Close", null);

      AlertDialog mAlertDialog = mBuilder.create();
      mAlertDialog.show();
    } catch (JSONException e) {
      HSLogger.e(TAG, "Failed to prettify JSON, setting original string", e);
    }
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
      default:
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

  private void startUserIdentityActivity() {
    Intent userIdentityIntent = new Intent(this, UserWithIdentityActivity.class);
    startActivity(userIdentityIntent);
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

  @Override
  protected void onPause() {
    super.onPause();
    HelpshiftEventsFlow.deregisterHelpshiftEventFlowListener();
  }

  @Override
  public void onNewEvent(HelpshiftEventData helpshiftEventData) {
    String eventName = helpshiftEventData.getEventName();
    Map<String, Object> data = helpshiftEventData.getData();

    if (storage.storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
      Toast.makeText(MainActivity.this, eventName + " " + data.toString(), Toast.LENGTH_SHORT).show();
    }
    Log.d(TAG, "eventName: " + eventName + " " + Utils.prettyFormatHashMap(data, 0));

    if (HelpshiftEvent.RECEIVED_UNREAD_MESSAGE_COUNT.equals(eventName)) {
      int count = (int) data.get(HelpshiftEvent.DATA_MESSAGE_COUNT);
      boolean fromCache = (boolean) data.get(HelpshiftEvent.DATA_MESSAGE_COUNT_FROM_CACHE);

      new AlertDialog.Builder(MainActivity.this)
              .setMessage("Count: " + count + ", From Cache: " + fromCache)
              .setCancelable(true)
              .show();
    }

    // Handle agent message received event
    if (HelpshiftEvent.AGENT_MESSAGE_RECEIVED.equals(eventName)) {
      handleAgentMessageReceivedEvent(data, eventName);
    }
  }
}
