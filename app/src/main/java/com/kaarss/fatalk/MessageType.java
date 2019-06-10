package com.kaarss.fatalk;

class MessageType {
    static int getAppSettings = 0;
    static int userSignUp = 1;
    static int userSignIn = 2;
    static int setSecurityAnswers = 3; // Task
    static int changePassword = 4; // Task
    static int verifySecurityAnswers = 5;
    static int getS3ParamsForDp = 6;
    static int updateFcmId = 7; // Task
    static int updateAgeGenderBio = 8; // Task
    static int updateDpVersion = 9; // Task
    static int updateUserBio = 10; // Task
    static int updateProfiles = 11;
    static int pendingTasks = 14;
    static int taskReceipt = 15;
    static int fcmGeneralNotification = 18; // Incoming Only
    static int fcmLinkNotification = 19; // Incoming Only
    static int fcmUpdateNotification = 20; // Incoming Only
    static int fetchLastSeenAt = 27;
    static int sendChatMessage = 28; // Task
    static int chatMessageSentAt = 29; // Incoming Only
    static int deliverChatMessage = 30; // Incoming Only
    static int chatMessageDeliveredAt = 31; // Both Incoming & Outgoing
    // Media Messages
    static int getS3ParamsForMedia = 32;
    // get Profiles
    static int getActiveUserProfiles = 33;
    // signal message downloaded to delete media from s3
    static int messageDownloaded = 34;
}
