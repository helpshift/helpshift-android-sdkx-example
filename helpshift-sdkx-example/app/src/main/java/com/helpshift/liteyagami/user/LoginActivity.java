package com.helpshift.liteyagami.user;

import static com.helpshift.liteyagami.mockUserAuthServer.MockBackendUserTokenServer.generateHMAC;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.helpshift.Helpshift;
import com.helpshift.liteyagami.MainActivity;
import com.helpshift.liteyagami.MainApplication;
import com.helpshift.liteyagami.R;
import com.helpshift.liteyagami.storage.StorageConstants;
import com.helpshift.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText userName,userId,userEmailId,userSecretKey;
    CheckBox enableAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getSupportActionBar().setTitle("Login");

        userName = findViewById(R.id.userNameTextView);
        userId = findViewById(R.id.userIdTextView);
        userEmailId = findViewById(R.id.userEmailTextView);
        enableAuthentication = findViewById(R.id.enableAuthBtn);
        enableAuthentication.setChecked(false);
        userSecretKey = findViewById(R.id.secretKeyTextView);
        final LinearLayout userAuthTokenInfo = findViewById(R.id.userAuthTokenInfo);

        final CheckBox clearAnonUserCheckbox = findViewById(R.id.clearAnonUserCheckBox);

        TextView secretKeyInfoText = findViewById(R.id.secretKeyInfoText);
        String infoText = "Secret key is used here for demo purposes. This key should NOT be included in your app's code. " +
                "You should generate the token from your own backend service using this secret key. " +
                "Refer <html> <a href=\"https://support.helpshift.com/kb/article/how-do-i-configure-the-endpoint-and-my-app-web-chat-widget-for-user-identity-verification/\">here</a> </html>";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            secretKeyInfoText.setText(Html.fromHtml(infoText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            secretKeyInfoText.setText(Html.fromHtml(infoText));
        }

        secretKeyInfoText.setMovementMethod(LinkMovementMethod.getInstance());

        enableAuthentication.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userAuthTokenInfo.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        userAuthTokenInfo.setVisibility(enableAuthentication.isChecked() ? View.VISIBLE : View.GONE);

        Button generateToken = findViewById(R.id.generateToken);
        generateToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enableAuthentication.isChecked()) {
                    String id = userId.getText().toString();
                    String email = userEmailId.getText().toString();
                    String secretKey = userSecretKey.getText().toString();
                    TextView userAuthTokenTextView = findViewById(R.id.userAuthTokenText);

                    String token = generateHMAC(id, email, secretKey);
                    if (Utils.isEmpty(token)) {
                        if (MainApplication.getAppStorage().storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
                            Toast.makeText(LoginActivity.this,
                                    "Error generating auth token. Check logs.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    userAuthTokenTextView.setText(token);
                }
            }
        });

        Button saveLogin = findViewById(R.id.saveLoginBtn);
        saveLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = userName.getText().toString();
                String email = userEmailId.getText().toString();
                String id = userId.getText().toString();
                String secretKey = userSecretKey.getText().toString();

                boolean loginSuccess = Helpshift.login(generateLoginData(id, name, email, secretKey));

                if (loginSuccess) {
                    if (MainApplication.getAppStorage().storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
                        Toast.makeText(LoginActivity.this, "Logged in:" + userName, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (MainApplication.getAppStorage().storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
                        Toast.makeText(LoginActivity.this, "Error in Login: Check logs", Toast.LENGTH_SHORT).show();
                    }
                }

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

        Button clearAnonymousUser = findViewById(R.id.clearAnonUser);
        clearAnonymousUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpshift.clearAnonymousUserOnLogin(clearAnonUserCheckbox.isChecked());
                String toast = clearAnonUserCheckbox.isChecked() ? "Anonymous User cleared" : "Anonymous User will be retained";
                if (MainApplication.getAppStorage().storageGetBoolean(StorageConstants.SHOW_TOAST_MESSAGE)) {
                    Toast.makeText(LoginActivity.this, toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Map<String,String> generateLoginData(final String id, final String name, final String email, final String secretKey) {
        Map<String, String> userData = new HashMap<>();
        userData.put("userId", id);
        userData.put("userEmail", email);
        userData.put("userName", name);

        if (enableAuthentication.isChecked()) {
            userData.put("userAuthToken", generateHMAC(id, email, secretKey));
        }
        return userData;
    }
}
