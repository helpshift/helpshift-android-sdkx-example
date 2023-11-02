package com.helpshift.liteyagami;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.helpshift.Helpshift;

public class SampleNotificationActivity extends AppCompatActivity {

    public static String EXTRA_CLOSE_SUPPORT_SESSION = "closeSupportSession";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_notification);

        handleIntent(getIntent());
    }

    void handleIntent(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_CLOSE_SUPPORT_SESSION, false)) {
            Helpshift.closeSession();
        }

        finish();
    }
}