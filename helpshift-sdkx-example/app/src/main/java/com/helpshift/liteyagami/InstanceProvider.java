package com.helpshift.liteyagami;

import com.helpshift.liteyagami.storage.AppStorage;

public class InstanceProvider {

    private static InstanceProvider instance;
    private static MainApplication mainApplication;
    private static AppStorage appStorage;

    public static synchronized void initInstance(MainApplication application) {
        if (instance == null) {
            instance = new InstanceProvider();
        }
        mainApplication = application;
        appStorage = new AppStorage(application);
    }

    public static InstanceProvider getInstance() {
        return instance;
    }

    public MainApplication getMainApplication() {
        return mainApplication;
    }

    public AppStorage getAppStorage() {
        return appStorage;
    }
}
