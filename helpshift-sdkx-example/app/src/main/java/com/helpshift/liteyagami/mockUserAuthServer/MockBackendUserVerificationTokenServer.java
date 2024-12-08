package com.helpshift.liteyagami.mockUserAuthServer;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A mock class to generate user authentication token.
 * This logic/code should be a backend service and should NOT be included in the app's code.
 * The Secret Key should reside on your backend so that it is secure and can be rotated whenever needed.
 */
public class MockBackendUserVerificationTokenServer {

    private MockBackendUserVerificationTokenServer() {
        // empty
    }

    /**
     * Logic to generate user auth token with given data.
     * This logic should be on the backend service of the app and should NOT be included with the app's code
     * @param userId
     * @param email
     * @param appSecretKey This key should reside on your backend. We have used it here only for demo purposes.
     * @return
     */
    public static String generateHMAC(String userId, String email, String appSecretKey){
        String parameter = "";

        if (userId != null && userId.length() != 0) {
            parameter += userId;
        }
        if (email != null && email.length() != 0) {
            parameter += email;
        }

        try {
            SecretKeySpec secretKey = new SecretKeySpec(appSecretKey.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmacData = mac.doFinal(parameter.getBytes("UTF-8"));
            return Base64.encodeToString(hmacData, Base64.NO_WRAP);
        }
        catch (Exception e) {
            Log.e("Helpshift", "Error generating token", e);
        }
        return "";
    }
}
