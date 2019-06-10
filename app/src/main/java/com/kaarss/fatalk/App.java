package com.kaarss.fatalk;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.facebook.stetho.Stetho;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class App extends Application implements LifecycleObserver {

    private static final String TAG = "Application";
    private static final Handler handler = new Handler(Looper.getMainLooper());

    // ----------- Shared Preferences -----
    public static SharedPreferences sharedPref;
    // ------ DB Daos ----
    public static UserProfileDao userProfileDao;
    public static TaskDao taskDao;
    public static ChatMessageDao chatMessageDao;

    public static Context applicationContext;

    public static boolean isForeground = false;

    public static List<Integer> chatMediaWithPreview = new ArrayList<>(Arrays.asList(Keys.chatMessageTypePicture,
            Keys.chatMessageTypeVideo,Keys.chatMessageTypeGif));
    public static List<Integer> chatMediaWithoutPreview = new ArrayList<>(Arrays.asList(Keys.chatMessageTypeAudio,Keys.chatMessageTypeDocument));

    @Override
    public void onCreate() {
        AppLog.d(TAG,"onCreate Called");
        super.onCreate();
        applicationContext = getApplicationContext();
        sharedPref = applicationContext.getSharedPreferences(
                AppPreferences.preferenceFileName, Context.MODE_PRIVATE);
        AppPreferences.initialize();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        // --- Db Daos ---
        LocalDatabase db = LocalDatabase.getDatabase(applicationContext);
        userProfileDao = db.userProfileDao();
        taskDao = db.taskDao();
        chatMessageDao = db.chatMessageDao();

        if (BuildConfig.DEBUG) {
            //Initialize stetho, debugging library
            Stetho.initializeWithDefaults(this);
            //disable firebase analytics
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
        }
        // Reset Any Uploads In Progress
        resetMediaUploadsInProgress();
        resetMediaDownloadsInProgress();
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appStopped() {
        isForeground = false;
        handler.removeCallbacks(checkMessages);
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appStarted() {
        AppLog.i(TAG,"App Started");
        isForeground = true;
        String userId = AppPreferences.getString(Keys.userId,"");
        if(!TextUtils.isEmpty(userId)){
            updateProfiles();
            checkMessages();
        }
    }
    private static Runnable checkMessages = new Runnable() {
        @Override
        public void run() {
            checkMessages();
        }
    };
    private static void checkMessages(){
        App.sendPendingTasks();
        long now = System.currentTimeMillis();
        long lsa = AppPreferences.getLong(Keys.lastRequestSentAT,now - 30000);
        if(now - lsa > 30000){
            Request.getPendingTasks();
        }
        checkPendingMediaUploads();
        handler.postDelayed(checkMessages,5000);
    }
    public static void sendMediaMessage(String messageId){
        new sendMediaMessage().execute(messageId);
    }
    private static class sendMediaMessage extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String messageId = strings[0];
            List<ChatMessage> chatMessages = chatMessageDao.getMessage(messageId);
            if(chatMessages.size() > 0){
                ChatMessage chatMessage = chatMessages.get(0);
                Request.sendChatMessage(chatMessage);
            }
            return null;
        }
    }
    public static void checkPendingMediaUploads(){
        new checkPendingMediaUploads().execute();
    }
    private static class checkPendingMediaUploads extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            List<ChatMessage> pendingUploads = chatMessageDao.getMediaMessagesToUpload();
            if(pendingUploads.size() > 0) {
                ChatMessage chatMessage = pendingUploads.get(0);
                Request.getS3ParamsForMedia(chatMessage.getMessageId());
            } else {
                AppLog.i(TAG,"No Pending Media Uploads");
            }
            return null;
        }
    }
    public static void downloadMedia(ChatMessage chatMessage){
        int mediaDownloadStatus = chatMessage.getMediaDownloadStatus();
        if(mediaDownloadStatus == Keys.mediaNotDownloaded){
            new downloadMediaFile().execute(chatMessage.getMessageId(),chatMessage.getType());
        }
    }
    public static class downloadMediaFile extends AsyncTask<Object,Void,Void>{
        @Override
        protected Void doInBackground(Object... objects) {
            String messageId = (String)objects[0];
            int messageType = (int)objects[1];
            chatMessageDao.setMediaDownloading(messageId);
            if(downloadFile(messageId)){
                // --- Create Preview Image -----
                File mediaFile = FileUtils.getMediaFile(messageId);
                if(messageType == Keys.chatMessageTypePicture) {
                    Bitmap imagePreview = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mediaFile.getPath()), 200, 200);
                    if(imagePreview != null){
                        FileUtils.saveMediaPreview(imagePreview, "preview_" + messageId);
                    }
                }
                if(messageType == Keys.chatMessageTypeVideo){
                    Bitmap videoPreview = FileUtils.createVideoThumbnail(mediaFile.getPath(),200,200);
                    if(videoPreview != null){
                        FileUtils.saveMediaPreview(videoPreview,"preview_"+messageId);
                    }
                }
                if(messageType == Keys.chatMessageTypeGif){
                    try (FileInputStream fis = new FileInputStream(mediaFile)) {
                        final GifDecoder gifDecoder = new GifDecoder();
                        gifDecoder.read(fis,0);
                        gifDecoder.advance();
                        Bitmap gifFrame = gifDecoder.getNextFrame();
                        if(gifFrame != null){
                            Bitmap gifPreview = ThumbnailUtils.extractThumbnail(gifFrame, 200, 200);
                            if(gifPreview != null){
                                FileUtils.saveMediaPreview(gifPreview,"preview_"+messageId);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // --- Signal Database to reflect the change in UI ------
                chatMessageDao.setMediaDownloaded(messageId);
                Request.messageDownloaded(messageId);
            } else {
                chatMessageDao.setMediaDownloadFailed(messageId);
            }
            return null;
        }
    }
    // ---------- Send Message On Fcm ---------
    public static void sendFcmMessage(boolean isTask, String taskId, Map request) {
        JSONObject json = new JSONObject();
        try {
            for (Object key : request.keySet()) {
                String keyS = (String) key;
                json.put(keyS, request.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String message = json.toString();
        if(isTask){
            insertTask(new Task(taskId,message));
        }
        FirebaseService.sendMessage(message);
    }
    // --- App directory ---
    public static File getDirectory(){
        File directory = applicationContext.getFilesDir();
        if(!directory.mkdirs()){
            AppLog.i(TAG,"Directory Was Not Created");
        };
        return directory;
    }
    // --------- Handler Tasks ------------
    private static class imageDownloadTask implements Runnable {
        private String userId;
        imageDownloadTask(String userId){
            this.userId = userId;
        }
        @Override
        public void run(){
            new downloadUserImage().execute(this.userId);
        }
    }
    //--------------- Images -------------------
    public static Bitmap getUserImage(String filename, int gender){
        File directory = App.getDirectory();
        // Create imageDir
        File f = new File(directory,filename);
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            new downloadUserImage().execute(filename);
        }
        if(b == null){
            b = AppPreferences.getDrawableBitmap(filename,gender);
        }
        return b;
    }
    public static Bitmap getMediaPreview(String uid){
        File directory = getDirectory();
        // Create imageDir
        File f = new File(directory,"preview_"+uid);
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            AppLog.e(TAG,"Preview Not Found:"+uid);
        }
        return b;
    }
    public static class downloadUserImage extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String userId = (String)strings[0];
            if(downloadFile(userId)) {
                EventBus.getDefault().post(new EventDownloadResult(userId,Keys.mediaTypeUser,true));
            }
            return null;
        }
    }
    public static boolean downloadFile(String uid){
        boolean downloaded = false;
        String bucket = AppPreferences.getString(Keys.s3Bucket,"");
        if(TextUtils.isEmpty(bucket)){
            AppLog.e(TAG,"Bucket Is Not Defined To Fetch Images");
            Request.getAppData();
            return false;
        }
        AppLog.d(TAG,"Downloading:"+uid);
        String urlString = "https://"+bucket+".s3.amazonaws.com/"+uid;
        File directory = getDirectory();
        File filePath = new File(directory, uid);
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.connect();
            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            // Output stream to write file
            OutputStream output = new FileOutputStream(filePath);
            byte[] data = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }
            // flushing output
            output.flush();
            // closing streams
            output.close();
            input.close();
            downloaded = true;
        } catch (Exception e) {
            AppLog.e(TAG,"Download Error:"+e.toString());
        }
        return downloaded;
    }
    // --- Upload Message Media ---
    public static void uploadMessageMedia(String messageId,String mediaParams){
        AppLog.i(TAG,"Checking:"+messageId);
        new uploadMessageMedia().execute(messageId,mediaParams);
    }
    private static class uploadMessageMedia extends AsyncTask<Object,Void,Void>{
        @Override
        protected Void doInBackground(Object... objects) {
            String messageId = (String)objects[0];
            String mediaParams = (String)objects[1];
            List<ChatMessage> chatMessages = App.chatMessageDao.getMessage(messageId);
            if(chatMessages.size() > 0){
                ChatMessage chatMessage =  chatMessages.get(0);
                int mediaUploadStatus = chatMessage.getMediaUploadStatus();
                if(mediaUploadStatus == Keys.mediaUploading){
                    AppLog.d(TAG,"Upload Already In Progress");
                } else if(mediaUploadStatus == Keys.mediaNotUploaded){
                    File mediaFile =  FileUtils.getMediaFile(messageId);
                    byte[] mediaFileBytes = new byte[(int) mediaFile.length()];
                    try (InputStream ios = new FileInputStream(mediaFile)) {
                        if (ios.read(mediaFileBytes) == -1) {
                            throw new IOException(
                                    "EOF reached while trying to read the whole file");
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        AppLog.i(TAG,"Uploading:"+messageId+"|Type:Media");
                        App.chatMessageDao.setMediaUploading(messageId);
                        new FileUpload(messageId,Keys.mediaTypeFile,mediaFileBytes,new JSONObject(mediaParams)).start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    //----------- DB Activities -----------
    public static void clearTable(String table){
        new clearTable().execute(table);
    }
    public static class clearTable extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params){
            String table = params[0];
            switch (table){
                case "profiles":
                    userProfileDao.clearTable();
                    break;
                case "messages":
                    chatMessageDao.clearTable();
                case "tasks":
                    taskDao.clearTable();
            }
            return null;
        }
    }
    public static void clearDatabase(){
        new clearDatabase().execute();
    }
    private static class clearDatabase extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            userProfileDao.clearTable();
            taskDao.clearTable();
            chatMessageDao.clearTable();
            return null;
        }
    }
    public static void insertProfile(UserProfile profile){
        new insertProfile().execute(profile);
    }
    private static class insertProfile extends AsyncTask<UserProfile, Void, Void> {
        @Override
        protected Void doInBackground(UserProfile... params) {
            UserProfile newProfile = params[0];
            String userId = newProfile.getUserId();
            List<UserProfile> oldProfiles = userProfileDao.getProfile(userId);
            if(oldProfiles.size() > 0){
                UserProfile oldProfile = oldProfiles.get(0);
                if(newProfile.getDpVersion() > 0){
                    if(newProfile.getDpVersion() != oldProfile.getDpVersion()){
                        handler.post(new imageDownloadTask(userId));
                    }
                }
            } else{
                if(newProfile.getDpVersion() > 0)handler.post(new imageDownloadTask(userId));
            }
            userProfileDao.insertProfile(newProfile);
            return null;
        }
    }
    public static class getProfile extends AsyncTask<String, Void, Object> {
        DbResponse delegate = null;
        @Override
        protected Object doInBackground(String... params) {
            String userId = params[0];
            List<UserProfile> profiles = userProfileDao.getProfile(userId);
            UserProfile profile = null;
            if (profiles.size() > 0) {
                profile = profiles.get(0);
            }
            return profile;
        }
        @Override
        protected void onPostExecute(Object data) {
            delegate.dbQueryResult(Keys.dbProfile, data);
        }
    }
    public static void setProfileDpBio(String userId,int dpVersion, String bio,long bioChangedAt){
        new setProfileDpBio().execute(userId,dpVersion,bio,bioChangedAt);
    }
    private static class setProfileDpBio extends AsyncTask<Object,Void,Void>{
        @Override
        protected Void doInBackground(Object... params){
            String userId = (String)params[0];
            int dpVersion = (int)params[1];
            String bio = (String)params[2];
            long bioChangedAt = (long)params[3];
            List<UserProfile> profiles = userProfileDao.getProfile(userId);
            if(profiles.size() > 0){
                UserProfile profile = profiles.get(0);
                if(dpVersion > profile.getDpVersion()){
                    handler.post(new imageDownloadTask(userId));
                }
                userProfileDao.setProfileDpBio(userId,dpVersion,bio,bioChangedAt);
            }
            return null;
        }
    }
    public static void deleteProfile(String uid){
        new deleteProfile().execute(uid);
    }
    private static class deleteProfile extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params){
            String uid = params[0];
            userProfileDao.deleteProfile(uid);
            applicationContext.deleteFile(uid);
            return null;
        }
    }
    public static void updateInteractedAt(String uid){new updateInteractedAt().execute(uid);}
    private static class updateInteractedAt extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params){
            String uid = params[0];
            userProfileDao.updateLastContactedAt(uid,System.currentTimeMillis());
            return null;
        }
    }
    public static void updateProfiles(){
        new updateProfiles().execute();
    }
    private static class updateProfiles extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params){
            List<String> items = userProfileDao.getAllProfiles();
            int totalItems = items.size();
            // -- Break The String To Accommodate 4KB Payload ----
            if(totalItems > 300){
                int iterations = (int)Math.ceil(totalItems/300.0);
                int start = 0;
                for(int i = 0;i < iterations; i++){
                    int end = totalItems < start + 300 ? totalItems : start + 300;
                    List<String> sublist = items.subList(start,end);
                    Request.updateProfiles(TextUtils.join(",",sublist));
                    start = end;
                }
            } else {
                Request.updateProfiles(TextUtils.join(",",items));
            }
            return null;
        }
    }
    public static void removeNonChatUsers(){new removeNonChatUsers().execute();}
    private static class removeNonChatUsers extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            userProfileDao.removeNonChatUsers();
            return null;
        }
    }
    // --------- Tasks --------------
    public static void insertTask(Task task){
        new insertTask().execute(task);
    }
    private static class insertTask extends AsyncTask<Task, Void, Void> {
        @Override
        protected Void doInBackground(Task... params) {
            Task newTask = params[0];
            taskDao.insertTask(newTask);
            return null;
        }
    }
    public static void deleteTask(String taskId){
        new deleteTask().execute(taskId);
    }
    private static class deleteTask extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... params){
            String taskId = params[0];
            taskDao.deleteTask(taskId);
            return null;
        }
    }
    public static void sendPendingTasks(){
        if(AppPreferences.getString(Keys.userId,"").isEmpty())return;
        new sendPendingTasks().execute();
    }
    private static class sendPendingTasks extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params){
            List<Task> tasks = taskDao.getTasks();
            for(Task task : tasks){
                String message = task.getData();
                FirebaseService.sendMessage(message);
            }
            return null;
        }
    }
    //-------------------- Chats ---------------------
    public static void insertMessage(ChatMessage message){
        new insertMessage().execute(message);
    }
    private static class insertMessage extends AsyncTask<ChatMessage, Void, Void> {
        @Override
        protected Void doInBackground(ChatMessage... params) {
            ChatMessage message = params[0];
            List<ChatMessage> messages = chatMessageDao.getMessage(message.getMessageId());
            if(messages.size() == 0){
                chatMessageDao.insertMessage(message);
                String dateString = TimeUtils.getDateOnlyString(message.getAddedAt()*1000);
                String dateId = message.getUserId()+dateString;
                int exist = chatMessageDao.getDateEntry(dateId);
                if(exist == 0){
                    long dateTime = TimeUtils.getMillisFromDate(dateString)/1000;
                    ChatMessage dateMessage = new ChatMessage(dateId,
                            message.getUserId(),
                            true,
                            dateString,
                            dateTime,
                            true,
                            Keys.chatMessageTypeDate,
                            "",
                            0,
                            "",
                            Keys.mediaStatusNA,
                            Keys.mediaStatusNA);
                    chatMessageDao.insertMessage(dateMessage);
                }
                if(!message.isMine()){
                    Request.chatMessageDeliveredAt(message.getUserId(),message.getMessageId(),System.currentTimeMillis()/1000);
                }
            }
            return null;
        }
    }
    public static void deleteMessage(String mid){ new deleteMessage().execute(mid);}
    private static class deleteMessage extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params) {
            String mid = (String) params[0];
            chatMessageDao.deleteMessage(mid);
            return null;
        }
    }
    public static void setMessagesRead(String uid){ new setMessagesRead().execute(uid);}
    private static class setMessagesRead extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... params) {
            String uid = (String) params[0];
            chatMessageDao.setMessagesRead(uid);
            return null;
        }
    }
    public static void setMessageSentAt(String messageId,long timeStamp){
        new setMessageSentAt().execute(messageId,timeStamp);
    }
    private static class setMessageSentAt extends AsyncTask<Object,Void,Void> {
        @Override
        protected Void doInBackground(Object... params) {
            String messageId = (String) params[0];
            long timeStamp = (long) params[1];
            chatMessageDao.setChatSentAt(messageId,timeStamp);
            return null;
        }
    }
    public static void setMessageDeliveredAt(String messageId,long timeStamp){
        new setMessageDeliveredAt().execute(messageId,timeStamp);
    }
    private static class setMessageDeliveredAt extends AsyncTask<Object,Void,Void> {
        @Override
        protected Void doInBackground(Object... params) {
            String messageId = (String) params[0];
            long timeStamp = (long) params[1];
            chatMessageDao.setChatDeliveredAt(messageId,timeStamp);
            return null;
        }
    }
    public static void setMessageReadByAt(String messageId,long timeStamp){
        new setMessageReadByAt().execute(messageId,timeStamp);
    }
    private static class setMessageReadByAt extends AsyncTask<Object,Void,Void> {
        @Override
        protected Void doInBackground(Object... params) {
            String messageId = (String) params[0];
            long timeStamp = (long) params[1];
            chatMessageDao.setChatReadByAt(messageId,timeStamp);
            return null;
        }
    }
    public static void resetMediaUploadsInProgress(){new resetMediaUploadsInProgress().execute();}
    private static class resetMediaUploadsInProgress extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void...params){
            chatMessageDao.resetMediaUploadsInProgress();
            return null;
        }
    }
    public static void resetMediaDownloadsInProgress(){new resetMediaDownloadsInProgress().execute();}
    private static class resetMediaDownloadsInProgress extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void...params){
            chatMessageDao.resetMediaDownloadsInProgress();
            return null;
        }
    }
    public static void setMediaUploaded(String messageId){
        new setMediaUploaded().execute(messageId);
    }
    private static class setMediaUploaded extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... objects) {
            String messageId = (String)objects[0];
            chatMessageDao.setMediaUploaded(messageId);
            return null;
        }
    }
    public static void setMediaUploadFailed(String messageId){
        new setMediaUploadFailed().execute(messageId);
    }
    private static class setMediaUploadFailed extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... objects) {
            String messageId = (String)objects[0];
            chatMessageDao.setMediaUploadFailed(messageId);
            return null;
        }
    }
}