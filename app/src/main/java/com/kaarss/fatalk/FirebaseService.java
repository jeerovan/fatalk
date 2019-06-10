package com.kaarss.fatalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class FirebaseService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    private static String NOTIFICATION_CHANNEL = "General Notifications";
    public static String ONGOING_CHANNEL = "Process Notification";

    private static final int generalNotificationId = 0;
    private static final int linkNotificationId = 1;
    private static final int versionNotificationId = 2;

    public static void createNotificationChannel(String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            if(channelName.equals(ONGOING_CHANNEL)){
                importance = NotificationManager.IMPORTANCE_LOW;
            }
            NotificationChannel channel = new NotificationChannel(channelName, name, importance);
            // Register the channel with the system
            getNotificationManager().createNotificationChannel(channel);
        }
    }

    public static void cancelNotification(int id) {
        getNotificationManager().cancel(id);
    }

    public static void showGeneralNotification(String title, String message) {
        createNotificationChannel(NOTIFICATION_CHANNEL);
        Intent intent = new Intent(App.applicationContext, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        showNotification(generalNotificationId, NOTIFICATION_CHANNEL, title, message, intent, true, true, false);
    }
    public static NotificationManager getNotificationManager() {
        return (NotificationManager) App.applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE);
    }
    public static void showNotification(int notificationId, String channelName, String title, String message, Intent intent, boolean autoCancel, boolean playSound, boolean ongoing) {
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.applicationContext, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(App.applicationContext,
                channelName)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(ongoing)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setAutoCancel(autoCancel)
                .setColor(ResourcesCompat.getColor(App.applicationContext.getResources(), R.color.colorPrimary, null))
                .setContentIntent(pendingIntent);
        if (playSound) notificationBuilder.setSound(sound);
        getNotificationManager().notify(notificationId, notificationBuilder.build());
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        AppLog.d("NEW_TOKEN",s);
        String oldFcmId = AppPreferences.getString(Keys.fcmId,"");
        String userId = AppPreferences.getString(Keys.userId,"");
        if(!oldFcmId.isEmpty() && !oldFcmId.equals(s) && !TextUtils.isEmpty(userId)){
            Request.updateFcmId(oldFcmId,s);
        }
        AppPreferences.setString(Keys.fcmId,s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            try {
                String messagesString = data.get(Keys.serverMessages);
                if(messagesString != null) {
                    JSONArray messages = new JSONArray(messagesString);
                    for (int i = 0; i < messages.length(); ++i) {
                        String messageString = messages.getString(i);
                        JSONObject message = new JSONObject(messageString);
                        Response.handle(message);
                        EventBus.getDefault().post(message);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    static void showLinkNotification(String title, String message, String Url) {
        createNotificationChannel(NOTIFICATION_CHANNEL);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        showNotification(linkNotificationId, NOTIFICATION_CHANNEL, title, message, intent, true, true, false);
    }

    static void showVersionNotification(String title, String message) {
        createNotificationChannel(NOTIFICATION_CHANNEL);
        String packageId = App.applicationContext.getPackageName();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageId));
        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = App.applicationContext.getPackageManager()
                .queryIntentActivities(intent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                intent.setComponent(componentName);
            }
        }
        showNotification(versionNotificationId, NOTIFICATION_CHANNEL, title, message, intent, true, true, false);
    }

    public static void sendMessage(String data) {
        String fcmId = AppPreferences.getString(Keys.fcmId,"");
        if(fcmId.isEmpty())return;
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        RemoteMessage.Builder rmb = new RemoteMessage.Builder(App.applicationContext.getResources().getString(R.string.fcm_sender_id));
        String tn = Long.toString(System.currentTimeMillis());
        rmb = rmb.setTtl(0)
                .setMessageId(tn)
                .addData("data", data);
        fm.send(rmb.build());
        AppLog.v(TAG,"SENT:"+data);
        AppPreferences.setLong(Keys.lastRequestSentAT,System.currentTimeMillis());
    }
}
