package com.helpshift.liteyagami.mockUserJWTTokenServer;

import static com.helpshift.util.Utils.isEmpty;
import static io.jsonwebtoken.Jwts.SIG.HS256;

import android.util.Log;

import com.helpshift.util.JsonUtils;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;

public class MockBackendUserJWTTokenServer {

  private static final String TAG = "MckBckndUsrJWTSrvr";
  private static String secretKey;
  private static Date iatDate;

  private MockBackendUserJWTTokenServer() {
    // empty
  }

  /**
   * Secret key is supposed to be stored in backend only but since we are mocking in demo app
   * we need to update it here from the app UI
   * @param secret
   */
  public static void initSecretKey(String secret) {
    secretKey = secret;
  }

  /**
   * In case we change iat date from demo app then update here
   * @param date
   */
  public static void setIATDate(Date date) {
    iatDate = date;
  }

  public static String generateJWTForUser(String userIdentityJSON) {
    try {
      if (isEmpty(userIdentityJSON)) {
        Log.d(TAG, "No identities added.");
        return "";
      }

      Map<String, Object> identities = JsonUtils.jsonStringToMap(userIdentityJSON);

      /**
       * Validate Identities for the user in the actual backend of your application and only
       * generate the JWT token
       */

      // IAT date should be within 24 hrs of current time. If not set, then set it now.
      if (iatDate == null) {
        Date date = new Date();
        date.setTime(System.currentTimeMillis() - 60 * 60 * 1000);
        iatDate = date;
      }

      SecretKey hashedSecretKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
      Header jwtHeader = Jwts.header().add("alg", "HS256").add("typ", "JWT").build();
      String identityJWTToken = Jwts.builder().header().add(jwtHeader).and().claims(identities).
                                    issuedAt(iatDate).signWith(hashedSecretKey, HS256).compact();

      Log.d(TAG, "identity JWT Token: " + identityJWTToken);
      return identityJWTToken;
    }
    catch (Exception e) {
      Log.e(TAG, "No Such Algorithm Found", e);
    }
    return "";
  }
}
