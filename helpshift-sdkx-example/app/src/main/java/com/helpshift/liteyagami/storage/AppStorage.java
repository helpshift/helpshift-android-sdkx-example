package com.helpshift.liteyagami.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppStorage {
  private final SharedPreferences storage;

  public AppStorage(Context c) {
    this.storage = PreferenceManager.getDefaultSharedPreferences(c);
  }

  public String storageGet(String key) {
    return storageGet(key, "");
  }

  public String storageGet(String key, String defaultValue) {
    return storage.getString(key, defaultValue);
  }

  public Boolean storageGetBoolean(String key) {
    return storage.getBoolean(key, false);
  }

  public Boolean storageGetBoolean(String key, boolean defaultVal) {
    return storage.getBoolean(key, defaultVal);
  }

  public void storageSet(String key, String data) {
    SharedPreferences.Editor editor = storage.edit();
    editor.putString(key, data);
    editor.commit();
  }

  public void storageSet(String key, Boolean data) {
    SharedPreferences.Editor editor = storage.edit();
    editor.putBoolean(key, data);
    editor.commit();
  }

  public void clear(String key){
    SharedPreferences.Editor editor = storage.edit();
    editor.remove(key);
    editor.commit();
  }
}
