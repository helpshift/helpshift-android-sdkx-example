package com.helpshift.liteyagami.util;

import static com.helpshift.liteyagami.util.StorageConstants.CURRENT_USER;
import static com.helpshift.util.Utils.getOrDefault;

import android.os.Build;
import android.widget.TextView;

import com.helpshift.liteyagami.InstanceProvider;
import com.helpshift.log.HSLogger;
import com.helpshift.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserUtils {

    private static String TAG = "Helpshift_UserUtils";

    public static <V> void storeUserInformation(String userType, Map<String, V> loginData) {
        try {
            JSONObject userInformation = new JSONObject();

            removeEmptyValues(loginData);

            userInformation.put("userType", userType);
            userInformation.put("loginData", loginData);

            InstanceProvider
                    .getInstance()
                    .getAppStorage()
                    .storageSet(CURRENT_USER, userInformation.toString());
        } catch (JSONException e) {
            HSLogger.e(TAG, "Failed to store sample user information", e);
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getUserInformation() {
        Map<String, String> userInformation = new HashMap<>();

        try {
            String userInformationString = InstanceProvider.getInstance().getAppStorage().storageGet(CURRENT_USER, "{}");
            JSONObject userInformationObject = new JSONObject(userInformationString);

            String userTypeString = userInformationObject.optString("userType", "");
            String loginDataString = userInformationObject.optString("loginData", "");

            userInformation.put("userType", userTypeString);
            userInformation.put("loginData", loginDataString);
        } catch (JSONException e) {
            HSLogger.e(TAG, "Failed to get sample app user information", e);
        }

        return userInformation;
    }

    public static <K, V> void removeEmptyValues(Map<K, V> map) {
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            V value = iterator.next().getValue();
            if (value instanceof String && Utils.isEmpty((String) value)) {
                iterator.remove();
            }
        }
    }

    public static void setUserInformation(TextView textUserType, TextView textUserInformation) {
        Map<String, String> userInformation = UserUtils.getUserInformation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                textUserType.setText(getOrDefault(userInformation,"userType", "AnonymousUser"));

                JSONObject userLoginData;
                String loginDataString = getOrDefault(userInformation, "loginData", "{}");

                if (!Utils.isEmpty(loginDataString)) {
                    userLoginData = new JSONObject(loginDataString);
                    textUserInformation.setText(userLoginData.toString());
                }
            } catch (JSONException e) {
                HSLogger.e(TAG, "Failed to set sample app information in view", e);
            }
        }
    }
}
