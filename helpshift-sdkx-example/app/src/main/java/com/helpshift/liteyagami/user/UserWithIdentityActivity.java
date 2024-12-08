package com.helpshift.liteyagami.user;

import static com.helpshift.liteyagami.util.StorageConstants.IDENTITY_TOKEN_KEY;
import static com.helpshift.liteyagami.util.StorageConstants.LOGIN_DATA_KEY;
import static com.helpshift.liteyagami.util.StorageConstants.JWT_SECRET_KEY;
import static com.helpshift.liteyagami.util.StorageConstants.USER_IDENTITIES_KEY;
import static com.helpshift.util.Utils.isEmpty;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.helpshift.Helpshift;
import com.helpshift.HelpshiftUserLoginEventsListener;
import com.helpshift.liteyagami.InstanceProvider;
import com.helpshift.liteyagami.R;
import com.helpshift.liteyagami.eventlistener.HSEventsFlowListener;
import com.helpshift.liteyagami.eventlistener.HelpshiftEventData;
import com.helpshift.liteyagami.eventlistener.HelpshiftEventsFlow;
import com.helpshift.liteyagami.mockUserJWTTokenServer.MockBackendUserJWTTokenServer;
import com.helpshift.liteyagami.storage.AppStorage;
import com.helpshift.liteyagami.util.StringUtils;
import com.helpshift.log.HSLogger;
import com.helpshift.util.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserWithIdentityActivity extends AppCompatActivity implements View.OnClickListener, HSEventsFlowListener {

    private static final String TAG = "UserIdentityActivity";

    EditText identifierKeyEditText, identifierValueEditText, valueKeyEditText, identityValueEditText,
            metaDataKeyEditText, metaDataValueEditText, userIdentitySecretKey, loginDataKeyEditText,
            loginDataValueEditText, iatEditText, clipboardIdentityToken, masterAttributeKeyEditText,
            masterAttibuteValueEditText, masterAttributeCUFKey, masterAttributeCUFValue,
            appAttributeKeyEditText, appAttibuteValueEditText, appAttributeCUFKey, appAttributeCUFValue;


    RadioGroup identityRadioGroup;

    RadioButton manualIdentityRadioButton, clipboardIdentityRadioButton;

    TextView identitiesText, loginDataPreview, loginResponse, iatValueTextView,
            masterAttributePreview, appAttributePreview, helpshiftEventsFlowTextView;

    LinearLayout clipboardIdentitySection, manualIdentitySection;

    private Date iatDate = new Date();

    private final HashMap<String, Object> userIdentities = new HashMap<>();
    List<Map<String, Object>> identitiesList = new ArrayList<>();
    private final HashMap<String, Object> metadata = new HashMap<>();
    private final HashMap<String, Object> loginData = new HashMap<>();

    private final HashMap<String, Object> masterAttributes = new HashMap<>();
    private final HashMap<String, String> masterCUF = new HashMap<>();

    private final HashMap<String, Object> appAttributes = new HashMap<>();
    private final HashMap<String, String> appCUF = new HashMap<>();

    private AppStorage appStorage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_identity);
        getSupportActionBar().setTitle("User Identity Login");

        appStorage = InstanceProvider.getInstance().getAppStorage();

        // Initialize UI components
        initializeUIComponents();

        // Fetch logged-in identity from storage
        fetchLoggedInIdentityFromStorage();

        // Set up the iat date
        setupIatDate();

        // Set up click listeners
        setupClickListeners(this);

        // Set up dropdown listeners
        setupDropdownListeners();

        // Set up radio group listener
        setupRadioGroupListener();

        // Set up Events Flow UI
        setupEventsFlowUI();
    }

    private void initializeUIComponents() {
        identifierKeyEditText = findViewById(R.id.identifierKey);
        identifierValueEditText = findViewById(R.id.identifierValue);
        valueKeyEditText = findViewById(R.id.valueKey);
        identityValueEditText = findViewById(R.id.valueValue);
        metaDataKeyEditText = findViewById(R.id.metaDataKey);
        metaDataValueEditText = findViewById(R.id.metaDataValue);
        identitiesText = findViewById(R.id.userIdentitiesJsonText);
        userIdentitySecretKey = findViewById(R.id.userSecretKey);
        loginDataKeyEditText = findViewById(R.id.loginDataKey);
        loginDataValueEditText = findViewById(R.id.loginDataValue);
        loginDataPreview = findViewById(R.id.loginDataPreview);
        loginResponse = findViewById(R.id.loginResponse);
        iatEditText = findViewById(R.id.iatKey);
        iatValueTextView = findViewById(R.id.iatValue);
        clipboardIdentityToken = findViewById(R.id.clipboardIdentityToken);
        masterAttributeKeyEditText = findViewById(R.id.masterAttributeKey);
        masterAttibuteValueEditText = findViewById(R.id.masterAttributeValue);
        masterAttributeCUFKey = findViewById(R.id.masterCufName);
        masterAttributeCUFValue = findViewById(R.id.masterCufValue);
        masterAttributePreview = findViewById(R.id.masterAttributeJsonText);
        appAttributeKeyEditText = findViewById(R.id.appAttributeKey);
        appAttibuteValueEditText = findViewById(R.id.appAttributeValue);
        appAttributeCUFKey = findViewById(R.id.appAttributeCufName);
        appAttributeCUFValue = findViewById(R.id.appAttributeCUFValue);
        appAttributePreview = findViewById(R.id.appAttributeJsonText);
        helpshiftEventsFlowTextView = findViewById(R.id.helpshiftEventsList);
        identityRadioGroup = findViewById(R.id.identityRadioGroup);
        manualIdentityRadioButton = findViewById(R.id.manualIdentityRadioButton);
        clipboardIdentityRadioButton = findViewById(R.id.clipboardIdentityRadioButton);
        manualIdentitySection = findViewById(R.id.manualIdentitySection);
        clipboardIdentitySection = findViewById(R.id.clipboardIdentitySection);
    }
    private void setupIatDate() {
        iatDate.setTime(System.currentTimeMillis() - 60 * 60 * 1000);
        iatValueTextView.setText(iatDate.getDate() + "-" + (iatDate.getMonth() + 1) + "-" + (iatDate.getYear() + 1900));
        MockBackendUserJWTTokenServer.setIATDate(iatDate);
    }

    private void setupClickListeners(UserWithIdentityActivity context) {
        findViewById(R.id.addIdentities).setOnClickListener(context);
        findViewById(R.id.loginWithIdentities).setOnClickListener(context);
        findViewById(R.id.resetIdentities).setOnClickListener(context);
        findViewById(R.id.addLoginData).setOnClickListener(context);
        findViewById(R.id.iatTime).setOnClickListener(context);
        findViewById(R.id.copyToken).setOnClickListener(context);
        findViewById(R.id.addUserIdentities).setOnClickListener(context);
        findViewById(R.id.addMetadata).setOnClickListener(context);
        findViewById(R.id.addMasterAttribute).setOnClickListener(context);
        findViewById(R.id.addMasterCUF).setOnClickListener(context);
        findViewById(R.id.masterAttribute).setOnClickListener(context);
        findViewById(R.id.addAppAttribute).setOnClickListener(context);
        findViewById(R.id.appAttributeAddCUf).setOnClickListener(context);
        findViewById(R.id.appAttribute).setOnClickListener(context);

        userIdentitySecretKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                appStorage.storageSet(JWT_SECRET_KEY, s.toString());
                MockBackendUserJWTTokenServer.initSecretKey(s.toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addIdentities:
                addIdentitiesToIdentitiesMap();
                break;
            case R.id.loginWithIdentities:
                loginWithIdentities();
                break;
            case R.id.resetIdentities:
                resetIdentities();
                break;
            case R.id.addLoginData:
                addLoginData();
                break;
            case R.id.iatTime:
                showDatePicker();
                break;
            case R.id.copyToken:
                copyTokenToClipboard();
                break;
            case R.id.addUserIdentities:
                Helpshift.addUserIdentities(createIdentityJwtToken());
                break;
            case R.id.addMetadata:
                addMetadata();
                break;
            case R.id.addMasterAttribute:
                addMasterAttribute();
                break;
            case R.id.addMasterCUF:
                addMasterCuf();
                break;
            case R.id.masterAttribute:
                updateMasterAttributes();
                break;
            case R.id.addAppAttribute:
                addAppAttribute();
                break;
            case R.id.appAttributeAddCUf:
                addAppCuf();
                break;
            case R.id.appAttribute:
                updateAppAttributes();
                break;
            default:
                break;
        }

    }

    private void updateAppAttributes() {
        Helpshift.updateAppAttributes(appAttributes);
        appAttributes.clear();
        appCUF.clear();
        updateAppAttributesOnUI();
    }

    private void addAppCuf() {
        String appCufName = appAttributeCUFKey.getText().toString().trim();
        String appCufValue = appAttributeCUFValue.getText().toString().trim();

        if (!isEmpty(appCufName) && !isEmpty(appCufValue)) {
            appCUF.put(appCufName, appCufValue);
        }
        if (!appCUF.isEmpty()) {
            appAttributes.put("custom_user_fields", appCUF);
        }
        updateAppAttributesOnUI();
        appAttributeCUFKey.setText("");
        appAttributeCUFValue.setText("");
    }

    private void addAppAttribute() {
        String appAttributeKey = appAttributeKeyEditText.getText().toString().trim();
        String appAttributeValue = appAttibuteValueEditText.getText().toString().trim();

        if (!isEmpty(appAttributeKey) && !isEmpty(appAttributeValue)) {
            if (appAttributeValue.contains(",")) {
                String[] attributeTags = appAttributeValue.split(",");
                appAttributes.put(appAttributeKey, attributeTags);
            } else {
                appAttributes.put(appAttributeKey, appAttributeValue);
            }
        }
        appAttributeKeyEditText.setText("");
        appAttibuteValueEditText.setText("");
        updateAppAttributesOnUI();
    }

    private void updateMasterAttributes() {
        Helpshift.updateMasterAttributes(masterAttributes);
        masterAttributes.clear();
        masterCUF.clear();
        updateMasterAttributesOnUI();

    }

    private void addMasterCuf() {
        String masterCufName = masterAttributeCUFKey.getText().toString().trim();
        String masterCufValue = masterAttributeCUFValue.getText().toString().trim();

        if (!isEmpty(masterCufName) && !isEmpty(masterCufValue)) {
            masterCUF.put(masterCufName, masterCufValue);
        }
        if (!masterCUF.isEmpty()) {
            masterAttributes.put("custom_user_fields", masterCUF);
        }
        updateMasterAttributesOnUI();
        masterAttributeCUFKey.setText("");
        masterAttributeCUFValue.setText("");

    }

    private void addMasterAttribute() {
        String masterAttributeKey = masterAttributeKeyEditText.getText().toString().trim();
        String masterAttributeValue = masterAttibuteValueEditText.getText().toString().trim();

        if (!isEmpty(masterAttributeKey) && !isEmpty(masterAttributeValue)) {

            if (masterAttributeValue.contains(",")) {
                String[] attributeTags = masterAttributeValue.split(",");
                masterAttributes.put(masterAttributeKey, attributeTags);
            } else {
                masterAttributes.put(masterAttributeKey, masterAttributeValue);
            }
        }
        masterAttributeKeyEditText.setText("");
        masterAttibuteValueEditText.setText("");
        updateMasterAttributesOnUI();
    }

    private void loginWithIdentities() {
        loginResponse.setText("In progress...");
        final String token = getCurrentSelectedIdentityToken();
        if (!userIdentities.isEmpty() && isEmpty(token)) {
            loginResponse.setText("Secret might be empty or token generation failed");
            return;
        }
        Helpshift.loginWithIdentity(token, loginData, new HelpshiftUserLoginEventsListener() {
            @Override
            public void onLoginSuccess() {
                storeIdentitiesToStorage(token);
                loginResponse.setText("User login with identity successful");
                loginResponse.setTextColor(Color.GREEN);
            }

            @Override
            public void onLoginFailure(String userLoginFailureReason, Map<String, String> map) {
                loginResponse.setText("Login with identity failed, reason: " + userLoginFailureReason + " data: " + map);
                loginResponse.setTextColor(Color.RED);
            }
        });
    }

    private void setupDropdownListeners() {
        String[] masterAttributesList = getResources().getStringArray(R.array.masterAttributes);
        String[] appAttributesList = getResources().getStringArray(R.array.appAttributes);
        setupDropdownListener(R.id.masterAttributesDropDown, masterAttributeKeyEditText, masterAttributesList);
        setupDropdownListener(R.id.appAttributesDropDown, appAttributeKeyEditText, appAttributesList);
    }

    private void setupDropdownListener(int dropdownId, final EditText editText, final String[] items) {
        ((Spinner) findViewById(dropdownId)).setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = items[position];
                editText.setText("NULL".equals(selectedValue) ? "" : selectedValue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(TAG, "Nothing selected from dropdown");
            }
        });
    }

    private void setupRadioGroupListener() {
        identityRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                manualIdentitySection.setVisibility(checkedId == R.id.manualIdentityRadioButton ? View.VISIBLE : View.GONE);
                clipboardIdentitySection.setVisibility(checkedId == R.id.clipboardIdentityRadioButton ? View.VISIBLE : View.GONE);
            }
        });
    }


    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(UserWithIdentityActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                try {
                    iatDate = simpleDateFormat.parse(selectedDate);
                    iatValueTextView.setText(selectedDate);
                    MockBackendUserJWTTokenServer.setIATDate(iatDate);
                } catch (Exception e) {
                    HSLogger.e(TAG, "Failed to parse date", e);
                    iatDate = new Date();
                }
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void copyTokenToClipboard() {
        String currentToken = getCurrentSelectedIdentityToken();
        if (isEmpty(currentToken)) {
            Log.d(TAG, "No Identity Added or Pasted");
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", currentToken);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetIdentities() {
        userIdentities.clear();
        identitiesList.clear();
        loginData.clear();
        updateLoginDataOnUI();
        updateIdentitiesOnUI();
        metadata.clear();
        clipboardIdentityToken.setText("");
        appStorage.storageSet(USER_IDENTITIES_KEY, "");
        appStorage.storageSet(IDENTITY_TOKEN_KEY, "");
        appStorage.storageSet(LOGIN_DATA_KEY, "");
    }

    private void addMetadata() {
        String metaDataKey = metaDataKeyEditText.getText().toString();
        String metaDataValue = metaDataValueEditText.getText().toString();

        if (!isEmpty(metaDataKey) || !isEmpty(metaDataValue)) {
            metadata.put(metaDataKey, metaDataValue);
        }
        clearTextFields(metaDataKeyEditText, metaDataValueEditText);
    }

    private void addLoginData() {
        String loginDataKey = loginDataKeyEditText.getText().toString().trim();
        String loginDataValue = loginDataValueEditText.getText().toString().trim();
        if (!isEmpty(loginDataKey) || !isEmpty(loginDataValue)) {
            if (StringUtils.isBooleanValue(loginDataValue)) {
                loginData.put(loginDataKey, Boolean.valueOf(loginDataValue));
            } else {
                loginData.put(loginDataKey, loginDataValue);
            }
        }
        updateLoginDataOnUI();
        clearTextFields(loginDataKeyEditText, loginDataValueEditText);
    }

    private void clearTextFields(EditText... fields) {
        for (EditText field : fields) {
            field.setText("");
        }
    }


    private void fetchLoggedInIdentityFromStorage() {
        try {
            String userIdentitiesKeyString = appStorage.storageGet(USER_IDENTITIES_KEY);
            String storedLoginDataString = appStorage.storageGet(LOGIN_DATA_KEY);

            JSONObject storedUserIdentities = new JSONObject(isEmpty(userIdentitiesKeyString) ? "{}" : userIdentitiesKeyString);
            JSONObject storedLoginData = new JSONObject(isEmpty(storedLoginDataString) ? "{}" : storedLoginDataString);

            identitiesText.setText("Logged In Identity Details\n");
            identitiesText.append(storedUserIdentities.toString(4));
            loginDataPreview.setText("Logged In User Details\n");
            loginDataPreview.append(storedLoginData.toString(4));

            clipboardIdentityToken.setText(appStorage.storageGet(IDENTITY_TOKEN_KEY));

            String storedSecret = appStorage.storageGet(JWT_SECRET_KEY);
            userIdentitySecretKey.setText(storedSecret);
            MockBackendUserJWTTokenServer.initSecretKey(storedSecret);
        } catch (Exception e) {
            HSLogger.e(TAG, "Failed to fetch from storage", e);
        }
    }

    private void storeIdentitiesToStorage(String jwtToken) {
        try {
            String userIdentitiesJson = JsonUtils.mapToJsonString(userIdentities);
            JSONObject userIdentitiesObject = new JSONObject(userIdentitiesJson);
            userIdentitiesObject.put("iat", iatDate.getTime() / 1000);
            appStorage.storageSet(USER_IDENTITIES_KEY, userIdentitiesObject.toString());
            appStorage.storageSet(IDENTITY_TOKEN_KEY, jwtToken);
            appStorage.storageSet(LOGIN_DATA_KEY, JsonUtils.mapToJsonString(loginData));
        } catch (Exception e) {
            HSLogger.e(TAG, "Failed to store identities", e);
        }
    }

    private void setupEventsFlowUI() {
        List<HelpshiftEventData> helpshiftEventsList = HelpshiftEventsFlow.getInstance().getHelpshiftEvents();
        for (HelpshiftEventData helpshiftEventData : helpshiftEventsList) {
            updateEventOnUI(helpshiftEventData);
        }
    }

    private void updateEventOnUI(HelpshiftEventData helpshiftEventData) {
        try {
            helpshiftEventsFlowTextView.append("\n");
            JSONObject event = new JSONObject();
            event.put("name", helpshiftEventData.getEventName());
            event.put("data", new JSONObject(helpshiftEventData.getData()));
            helpshiftEventsFlowTextView.append(event.toString(4));
        } catch (JSONException e) {
            helpshiftEventsFlowTextView.append(helpshiftEventData.toString());
            showFailedJsonError(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        HelpshiftEventsFlow.setHelpshiftEventFlowListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        HelpshiftEventsFlow.deregisterHelpshiftEventFlowListener();
    }

    private void addIdentitiesToIdentitiesMap() {
        String identifierKey = identifierKeyEditText.getText().toString();
        String identifierValue = identifierValueEditText.getText().toString();

        String valueKey = valueKeyEditText.getText().toString();
        String identityValue = identityValueEditText.getText().toString();

        if (isEmpty(identifierKey) || isEmpty(identifierValue) || isEmpty(valueKey) || isEmpty(identityValue)) {
            return;
        }

        Map<String, Object> identity = new HashMap<>();
        identity.put(identifierKey, identifierValue);
        identity.put(valueKey, identityValue);

        if (!metadata.isEmpty()) {
            identity.put("metadata", new HashMap<>(metadata));
        }

        addIdentityToIdentitiesList(identity);
        updateIdentitiesOnUI();

        identityValueEditText.setText("");
        identifierValueEditText.setText("");
        metaDataKeyEditText.setText("");
        metaDataValueEditText.setText("");

        metadata.clear();
    }

    private String getCurrentSelectedIdentityToken() {
        if (identityRadioGroup.getCheckedRadioButtonId() == R.id.manualIdentityRadioButton) {
            return createIdentityJwtToken();
        }

        return String.valueOf(clipboardIdentityToken.getText());
    }

    private void updateLoginDataOnUI() {
        try {
            JSONObject jsonObject = new JSONObject(JsonUtils.mapToJsonString(loginData));
            loginDataPreview.setText(jsonObject.toString(4));
        } catch (Exception e) {
            loginDataPreview.setText(JsonUtils.mapToJsonString(loginData));
            showFailedJsonError(e);
        }
    }

    private String createIdentityJwtToken() {

        if (userIdentities.isEmpty()) {
            HSLogger.d(TAG, "No identities added.");
            return "";
        }

        String secretKey = userIdentitySecretKey.getText().toString();
        if (isEmpty(secretKey)) {
            loginResponse.setText("Secret key is empty");
            return "";
        }

        String token = MockBackendUserJWTTokenServer.generateJWTForUser(JsonUtils.mapToJsonString(userIdentities));
        if (isEmpty(token)) {
            loginResponse.setText("Error generating token...");
        }
        return token;
    }

    private void addIdentityToIdentitiesList(Map<String, Object> identity) {
        identitiesList.add(identity);
        userIdentities.put("identities", identitiesList);
    }

    private void updateIdentitiesOnUI() {
        try {
            JSONObject jsonObject = new JSONObject(JsonUtils.mapToJsonString(userIdentities));
            jsonObject.put("iat", iatDate.getTime() / 1000);
            identitiesText.setText(jsonObject.toString(4));
        } catch (Exception e) {
            identitiesText.setText(JsonUtils.mapToJsonString(userIdentities));
            showFailedJsonError(e);
        }
    }

    private static void showFailedJsonError(Exception e) {
        HSLogger.e(TAG, "Failed to prettify JSON, setting original string", e);
    }

    private void updateMasterAttributesOnUI() {
        try {
            JSONObject jsonObject = new JSONObject(JsonUtils.mapToJsonString(masterAttributes));
            masterAttributePreview.setText(jsonObject.toString(4));
        } catch (JSONException e) {
            masterAttributePreview.setText(JsonUtils.mapToJsonString(masterAttributes));
            showFailedJsonError(e);
        }
    }

    private void updateAppAttributesOnUI() {
        try {
            JSONObject jsonObject = new JSONObject(JsonUtils.mapToJsonString(appAttributes));
            appAttributePreview.setText(jsonObject.toString(4));
        } catch (JSONException e) {
            appAttributePreview.setText(JsonUtils.mapToJsonString(appAttributes));
            showFailedJsonError(e);
        }
    }

    @Override
    public void onNewEvent(HelpshiftEventData data) {
        updateEventOnUI(data);
    }
}
