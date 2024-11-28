package com.helpshift.liteyagami.util;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Random;

public class NotificationUtils {

    public static int SESSION_CLOSE_NOTIFICATION_ID = 1001;
    public static int PROACTIVE_NOTIFICATION_ID = 1002;
    public static int NOTIFICATION_ID = 1002;

    private NotificationUtils() {
        // empty
    }

    public static void showNotification(Context context,
                                        Intent intent,
                                        String channelId,
                                        int notificationId,
                                        String title, String message,
                                        int icon, boolean isAutoCancellable){
        if (context == null) {
            return;
        }

        int pendingIntentFlag = Build.VERSION.SDK_INT < 23 ? 0 : PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, new Random().nextInt(), intent, pendingIntentFlag);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(isAutoCancellable)
                .setChannelId(channelId);

        if (builder != null) {
            Notification notification = builder.build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, notification);
        }
    }
}
