package com.helpshift.liteyagami.user;

import static com.helpshift.liteyagami.mockUserAuthServer.MockBackendUserVerificationTokenServer.generateHMAC;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.helpshift.Helpshift;
import com.helpshift.liteyagami.R;
import com.helpshift.liteyagami.util.UserUtils;
import com.helpshift.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText userName;
    EditText userId;
    EditText userEmailId;
    EditText userSecretKey;
    CheckBox enableAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.userNameTextView);
        userId = findViewById(R.id.userIdTextView);
        userEmailId = findViewById(R.id.userEmailTextView);
        enableAuthentication = findViewById(R.id.enableAuthBtn);
        enableAuthentication.setChecked(false);
        userSecretKey = findViewById(R.id.secretKeyTextView);
        final LinearLayout userAuthTokenInfo = findViewById(R.id.userAuthTokenInfo);

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

        enableAuthentication.setOnCheckedChangeListener(
            (buttonView, isChecked) -> userAuthTokenInfo.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        userAuthTokenInfo.setVisibility(enableAuthentication.isChecked() ? View.VISIBLE : View.GONE);

        Button generateToken = findViewById(R.id.generateToken);
        generateToken.setOnClickListener(v -> {
            if (enableAuthentication.isChecked()) {
                String id = userId.getText().toString();
                String email = userEmailId.getText().toString();
                String secretKey = userSecretKey.getText().toString();
                TextView userAuthTokenTextView = findViewById(R.id.userAuthTokenText);

                String token = generateHMAC(id, email, secretKey);
                if (Utils.isEmpty(token)) {
                    Toast.makeText(LoginActivity.this,
                            "Error generating auth token. Check logs.",
                            Toast.LENGTH_SHORT).show();
                }

                userAuthTokenTextView.setText(token);
            }
        });

        Button saveLogin = findViewById(R.id.saveLoginBtn);
        saveLogin.setOnClickListener(view -> {
            String name = userName.getText().toString();
            String email = userEmailId.getText().toString();
            String id = userId.getText().toString();
            String secretKey = userSecretKey.getText().toString();

            Map<String, String> loginData = generateLoginData(id, name, email, secretKey);
            boolean loginSuccess = Helpshift.login(loginData);

            if (loginSuccess) {
                UserUtils.storeUserInformation(UserType.OLD_LOGIN_USER, loginData);
                Toast.makeText(LoginActivity.this, "Logged in:" + userName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Error in Login: Check logs", Toast.LENGTH_SHORT).show();
            }

            onBackPressed();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
