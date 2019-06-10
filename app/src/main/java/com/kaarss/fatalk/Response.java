package com.kaarss.fatalk;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {
    private static String TAG = Response.class.getSimpleName();
    public static void handle(JSONObject message) throws JSONException {
        AppLog.d("FCM","RECEIVED: " + message.toString());
        int messageType = message.getInt(Keys.messageType);
        switch (messageType){
            case 0:
                String s3Bucket = message.getString(Keys.s3Bucket);
                long generalRequestTimeoutMillis = message.getLong(Keys.generalRequestTimeoutMillis);
                long s3ParamsExpirySeconds = message.getLong(Keys.s3ParamsExpirySeconds);
                AppPreferences.setString(Keys.s3Bucket,s3Bucket);
                AppPreferences.setLong(Keys.generalRequestTimeoutMillis,generalRequestTimeoutMillis);
                AppPreferences.setLong(Keys.s3ParamsExpirySeconds,s3ParamsExpirySeconds);
                break;
            case 1:
                int rs1 = message.getInt(Keys.responseStatus);
                if(rs1 == 1){
                    String userId = message.getString(Keys.userId);
                    AppPreferences.setString(Keys.userId,userId);
                    // ---- Set Defaults -----
                    AppPreferences.setInt(Keys.userState,1); // set security
                    AppPreferences.setString(Keys.securityState,Keys.settingSecurity);
                }
                break;
            case 2:
                int rs2 = message.getInt(Keys.responseStatus);
                if(rs2 == 1){
                    AppPreferences.setUser(message);
                }
                break;
            case 5:
                int rs5 = message.getInt(Keys.responseStatus);
                if(rs5 == 1){
                    AppPreferences.setUser(message);
                }
                break;
            case 11:
                JSONArray profilesData = new JSONArray(message.getString(Keys.profilesData));
                for(int i = 0;i < profilesData.length(); i++){
                    String profileData = profilesData.getString(i);
                    JSONObject profile = new JSONObject(profileData);
                    String userId = profile.getString(Keys.userId);
                    int dpVersion = profile.getInt(Keys.dpVersion);
                    String bioString = profile.getString(Keys.userBio);
                    long bioChangedAt = profile.getLong(Keys.bioChangedAt);
                    String bio = bioString.isEmpty() ? "No Bio" : bioString;
                    App.setProfileDpBio(userId,dpVersion,bio,bioChangedAt);
                }
                break;
            case 14: // Handle Pending Tasks
                int rs14 = message.getInt(Keys.responseStatus);
                if(rs14 == 1) {
                    String tasksString = message.getString(Keys.tasks);
                    JSONArray tasksArray = new JSONArray(tasksString);
                    for (int i = 0; i < tasksArray.length(); ++i) {
                        String taskString = tasksArray.getString(i);
                        JSONObject taskMessage = new JSONObject(taskString);
                        Response.handle(taskMessage);
                        EventBus.getDefault().post(taskMessage);
                    }
                }
                break;
            case 15: // Handle Task Receipt
                int rs15 = message.getInt(Keys.responseStatus);
                if(rs15 == 1) {
                    String taskId = message.getString(Keys.taskId);
                    App.deleteTask(taskId);
                }
                break;
            case 18:
                String notificationTitle18 = message.getString(Keys.fcmNotificationTitle);
                String notificationMessage18 = message.getString(Keys.fcmNotificationMessage);
                FirebaseService.showGeneralNotification(notificationTitle18,notificationMessage18);
                break;
            case 19:
                String notificationTitle19 = message.getString(Keys.fcmNotificationTitle);
                String notificationMessage19 = message.getString(Keys.fcmNotificationMessage);
                String notificationUrl = message.getString(Keys.fcmNotificationUrl);
                FirebaseService.showLinkNotification(notificationTitle19,notificationMessage19,
                        notificationUrl);
                break;
            case 20:
                String notificationTitle20 = message.getString(Keys.fcmNotificationTitle);
                String notificationMessage20 = message.getString(Keys.fcmNotificationMessage);
                int notificationVersion = message.getInt(Keys.fcmNotificationVersion);
                if (BuildConfig.VERSION_CODE < notificationVersion) {
                    FirebaseService.showVersionNotification(notificationTitle20,notificationMessage20);
                }
                break;
            case 29: // Chat Message Sent To Server At
                int rs29 = message.getInt(Keys.responseStatus);
                if(rs29 == 1){
                    String chatMessageId = message.getString(Keys.chatMessageId);
                    String taskId = message.getString(Keys.taskId);
                    long timeStamp = message.getLong(Keys.timeStamp);
                    App.deleteTask(taskId);
                    App.setMessageSentAt(chatMessageId,timeStamp);
                }
                break;
            case 30: // Chat Message Delivery From Another User
                int rs30 = message.getInt(Keys.responseStatus);
                if(rs30 == 1){
                    int chatType = message.getInt(Keys.chatMessageType);
                    int mediaDownloadStatus = Keys.mediaStatusNA;
                    if(App.chatMediaWithoutPreview.contains(chatType) || App.chatMediaWithPreview.contains(chatType)){
                        mediaDownloadStatus = Keys.mediaNotDownloaded;
                    }
                    ChatMessage chatMessage = new ChatMessage(
                            message.getString(Keys.chatMessageId),
                            message.getString(Keys.userId),
                            false,
                            message.getString(Keys.chatMessage),
                            System.currentTimeMillis()/1000,
                            false,
                            chatType,
                            message.getString(Keys.mediaName),
                            message.getInt(Keys.mediaSize),
                            message.getString(Keys.mediaData),
                            Keys.mediaStatusNA,
                            mediaDownloadStatus
                    );
                    App.insertMessage(chatMessage); // We Send Deliver Receipt After Inserting
                    String taskId = message.getString(Keys.taskId);
                    Request.taskReceipt(taskId);
                    if(!App.isForeground)FirebaseService.showGeneralNotification("New Messages","Tap Here To View");
                }
                break;
            case 31: //Chat Message Delivered At - Sent By Me
                int rs31 = message.getInt(Keys.responseStatus);
                if(rs31 == 1){
                    String chatMessageId = message.getString(Keys.chatMessageId);
                    long timeStamp = message.getLong(Keys.timeStamp);
                    App.setMessageDeliveredAt(chatMessageId,timeStamp);
                    String taskId = message.getString(Keys.taskId);
                    Request.taskReceipt(taskId);
                }
                break;
            case 32:
                int rs32 = message.getInt(Keys.responseStatus);
                if(rs32 == 1){
                    String chatMessageId = message.getString(Keys.chatMessageId);
                    String mediaParamString = message.getString(Keys.mediaS3Params);
                    App.uploadMessageMedia(chatMessageId,mediaParamString);
                }
                break;
            case 33:
                int rs33 = message.getInt(Keys.responseStatus);
                if(rs33 == 1){
                    JSONArray userProfiles = new JSONArray(message.getString(Keys.profilesData));
                    for(int i = 0;i < userProfiles.length(); i++){
                        String profileData = userProfiles.getString(i);
                        JSONObject profile = new JSONObject(profileData);
                        UserProfile userProfile = new UserProfile(profile.getString(Keys.userId),
                                profile.getString(Keys.userName),
                                profile.getString(Keys.country),
                                profile.getInt(Keys.userAge),
                                profile.getInt(Keys.userGender),
                                profile.getInt(Keys.dpVersion),
                                profile.getString(Keys.userBio),
                                profile.getLong(Keys.bioChangedAt));
                        App.insertProfile(userProfile);
                    }
                }
                break;
            default:
                break;
        }
    }
}
