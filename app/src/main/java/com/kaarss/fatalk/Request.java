package com.kaarss.fatalk;

import androidx.collection.ArrayMap;
import android.text.TextUtils;

import java.util.Map;

public class Request {
    private static final String TAG = Request.class.getSimpleName();

    static void getAppData(){
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.messageType,MessageType.getAppSettings);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        App.sendFcmMessage(false,"",message);
    }
    static void signUp(String userName,String passWord,String country){
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.userSignUp);
        message.put(Keys.userName,userName );
        message.put(Keys.passWord,passWord);
        message.put(Keys.country,country);
        App.sendFcmMessage(false,"",message);
    }
    static void signIn(String userName,String passWord){
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.userSignIn);
        message.put(Keys.userName,userName );
        message.put(Keys.passWord,passWord);
        App.sendFcmMessage(false,"",message);
    }
    static void setSecurity(){
        String taskId = MessageType.setSecurityAnswers+"";
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.setSecurityAnswers);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.answerOne,AppPreferences.getString(Keys.answerOne,""));
        message.put(Keys.answerTwo,AppPreferences.getString(Keys.answerTwo,""));
        message.put(Keys.answerThree,AppPreferences.getString(Keys.answerThree,""));
        message.put(Keys.answerFour,AppPreferences.getString(Keys.answerFour,""));
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
    static void changePassword(String password){
        String taskId = MessageType.changePassword+"";
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.changePassword);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.passWord,password);
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
    static void verifySecurity(){
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.verifySecurityAnswers);
        message.put(Keys.userName,AppPreferences.getString(Keys.userName,""));
        message.put(Keys.answerOne,AppPreferences.getString(Keys.answerOne,""));
        message.put(Keys.answerTwo,AppPreferences.getString(Keys.answerTwo,""));
        message.put(Keys.answerThree,AppPreferences.getString(Keys.answerThree,""));
        message.put(Keys.answerFour,AppPreferences.getString(Keys.answerFour,""));
        App.sendFcmMessage(false,"",message);
    }
    static void getS3ParamsForDp(){
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.getS3ParamsForDp);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        App.sendFcmMessage(false,"",message);
    }
    static void updateFcmId(String oldFcmId,String newFcmId){
        String taskId = MessageType.updateFcmId+"";
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.updateFcmId);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.oldFcmId,oldFcmId);
        message.put(Keys.newFcmId,newFcmId);
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
    static void updateAgeGenderBio(int age, int gender,String bio){
        String taskId = MessageType.updateAgeGenderBio+"";
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.updateAgeGenderBio);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.userAge,age);
        message.put(Keys.userGender,gender);
        message.put(Keys.userBio,bio);
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
    static void updateDpVersion(){
        String taskId = MessageType.updateDpVersion+"";
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.messageType,MessageType.updateDpVersion);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
    static void updateBio(String bio){
        String taskId = MessageType.updateUserBio+"";
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.messageType,MessageType.updateUserBio);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.userBio,bio);
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
    static void updateProfiles(String profilesData){
        if(TextUtils.isEmpty(profilesData))return;
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.messageType,MessageType.updateProfiles);
        message.put(Keys.profilesData,profilesData);
        App.sendFcmMessage(false,"",message);
    }
    static void taskReceipt(String taskId){
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.messageType,MessageType.taskReceipt);
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(false,"",message);
    }
    static void getPendingTasks(){
        String userId = AppPreferences.getString(Keys.userId,"");
        if(userId.isEmpty())return;
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.userId,userId);
        message.put(Keys.messageType,MessageType.pendingTasks);
        App.sendFcmMessage(false,"",message);
    }
    static void lastSeenAt(String toUserId){
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.toUserId,toUserId);
        message.put(Keys.messageType,MessageType.fetchLastSeenAt);
        App.sendFcmMessage(false,"",message);
    }
    static void sendChatMessage(ChatMessage chatMessage){
        String taskId = chatMessage.getMessageId() + MessageType.sendChatMessage;
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.messageType,MessageType.sendChatMessage);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.toUserId,chatMessage.getUserId());
        message.put(Keys.chatMessage,chatMessage.getMessage());
        message.put(Keys.chatMessageId,chatMessage.getMessageId());
        message.put(Keys.chatMessageType,chatMessage.getType());
        // Media Params
        message.put(Keys.mediaName,chatMessage.getMediaName());
        message.put(Keys.mediaSize,chatMessage.getMediaSize());
        message.put(Keys.mediaData,chatMessage.getMediaData());
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
    static void chatMessageDeliveredAt(String toUserId,String chatMessageId,long timeStamp){
        String taskId = chatMessageId + MessageType.chatMessageDeliveredAt;
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.toUserId,toUserId);
        message.put(Keys.chatMessageId,chatMessageId);
        message.put(Keys.timeStamp,timeStamp);
        message.put(Keys.messageType,MessageType.chatMessageDeliveredAt);
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
    static void getS3ParamsForMedia(String messageId){
        Map<String, Object> message = new ArrayMap<>();
        message.put(Keys.messageType, MessageType.getS3ParamsForMedia);
        message.put(Keys.chatMessageId,messageId);
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        App.sendFcmMessage(false,"",message);
    }
    static void messageDownloaded(String messageId){
        String taskId = messageId + MessageType.messageDownloaded;
        Map<String,Object> message = new ArrayMap<>();
        message.put(Keys.userId,AppPreferences.getString(Keys.userId,""));
        message.put(Keys.chatMessageId,messageId);
        message.put(Keys.messageType,MessageType.messageDownloaded);
        message.put(Keys.taskId,taskId);
        App.sendFcmMessage(true,taskId,message);
    }
}
