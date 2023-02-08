package com.helpshift.liteyagami.external;

import com.helpshift.liteyagami.MainActivity;

public interface InternalFeatures {

    void init();

    void handleClicks(int clickedId);

    void setSdkUrls(String newWebchatUrl, String newHCUrl, boolean newHcIsSandBox);
}
