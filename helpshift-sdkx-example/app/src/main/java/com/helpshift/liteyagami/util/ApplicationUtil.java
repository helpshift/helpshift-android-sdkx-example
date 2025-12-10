package com.helpshift.liteyagami.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class ApplicationUtil {

  public static void copyToClipboard(Context context, String text) {
    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("label", text);
    if (clipboard != null) {
      clipboard.setPrimaryClip(clip);
      Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
  }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createChannel(Context context, String channelId, Integer soundResourceId, String description) {
        NotificationManager notificationManager = com.helpshift.util.ApplicationUtil.getNotificationManager(context);
        if (notificationManager != null) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
            //Notification channel not exist so create new one
            if (notificationChannel == null) {
                //Create the channel with default ID
                NotificationChannel mChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
                mChannel.setDescription(description);

                Uri soundUri = getNotificationSoundUri(context, soundResourceId);
                if (soundUri != null) {
                    mChannel.setSound(soundUri, new AudioAttributes.Builder().build());
                }
                notificationManager.createNotificationChannel(mChannel);
            }
        }
    }

    public static Uri getNotificationSoundUri(Context context, int notificationSoundId) {
        Uri soundUri = null;
        if (notificationSoundId != 0) {
            String soundUriString = "android.resource://" + context.getPackageName() + "/" + notificationSoundId;
            soundUri = Uri.parse(soundUriString);
        }
        return soundUri;
    }
}
