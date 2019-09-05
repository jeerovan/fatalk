package com.kaarss.fatalk;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityChat extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = ActivityChat.class.getSimpleName();
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private final int FILE_SELECT_CODE = 2385;

    CircleImageView _profileImage;
    TextView _userName;
    TextView _lastAvailable;
    FrameLayout _noChats;
    EditText _chatMessage;
    ImageView _sendMessage;
    ImageView _attachment;
    RecyclerView recycler;

    String userId;
    String userName;

    public static View.OnClickListener chatItemClickListener;
    public static View.OnLongClickListener chatItemLongClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final Intent intent = getIntent();
        userId = intent.getStringExtra(Keys.userId);
        userName = intent.getStringExtra(Keys.userName);

        _profileImage = findViewById(R.id.profile_image);
        _userName = findViewById(R.id.username);
        _lastAvailable = findViewById(R.id.last_available);
        _noChats = findViewById(R.id.no_chat);
        _chatMessage = findViewById(R.id.chat_message);
        _sendMessage = findViewById(R.id.send_message);
        _attachment =  findViewById(R.id.attach_message);
        recycler = findViewById(R.id.recycler);

        _lastAvailable.setVisibility(View.GONE);
        Request.lastSeenAt(userId);

        _chatMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int characters = s.length();
                if(characters > 300){
                    _chatMessage.setError(characters+" Characters");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        _attachment.setOnClickListener(this);
        _sendMessage.setOnClickListener(this);

        chatItemClickListener = new ChatItemClickListener();
        chatItemLongClickListener = new ChatItemLongClickListener();

        final AdapterChat adapterChat = new AdapterChat(this);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(linearLayoutManager);
        recycler.setAdapter(adapterChat);
        recycler.setItemAnimator(null);

        App.daoChatMessage.getMessages(userId).observe(this,(chatMessages -> {
            assert chatMessages != null;
            if(chatMessages.size() == 0){
                _noChats.setVisibility(View.VISIBLE);
            } else {
                _noChats.setVisibility(View.GONE);
            }
            App.setMessagesRead(userId);
            adapterChat.setMessages(chatMessages);
        }));
    }
    @Override
    protected void onStart(){
        _profileImage.setImageBitmap(App.getUserImage(userId, AppPreferences.getInt(Keys.userGender,0)));
        _userName.setText(userName);
        EventBus.getDefault().register(this);
        scrollChat();
        super.onStart();
    }
    @Override
    protected void onStop(){
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.attach_message:
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                try {
                    startActivityForResult(
                            Intent.createChooser(fileIntent, "Select a File to Send"),
                            FILE_SELECT_CODE);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, "Please install a File Manager.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.send_message:
                String message = _chatMessage.getText().toString().trim().replaceAll(" +"," ");
                if(!TextUtils.isEmpty(message) && message.length() < 301) {
                    sendMessage(message);
                    _chatMessage.getText().clear();
                }
                break;
        }
    }
    private void scrollChat(){
        recycler.postDelayed(() -> {recycler.scrollToPosition(0);},100);
    }
    private void sendMessage(String message){
        long addedAt = System.currentTimeMillis()/1000;
        String messageId = AppPreferences.getString(Keys.userId,"")+ userId + addedAt;
        ChatMessage chatMessage = new ChatMessage(messageId,
                userId,
                true,
                message,
                addedAt,
                true,
                Keys.chatMessageTypeText,
                "",
                0,
                "",
                Keys.mediaStatusNA,
                Keys.mediaStatusNA);
        App.insertMessage(chatMessage);
        Request.sendChatMessage(chatMessage);
        scrollChat();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            if(data.getData() != null) {
                Uri uri = data.getData();
                handleAttachment(uri);
            } else if(data.getClipData() != null){
                int count = data.getClipData().getItemCount();
                for(int i = 0;i < count;i++){
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    handleAttachment(uri);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private static void showErrorToast(String error){
        Toast.makeText(App.applicationContext,error,Toast.LENGTH_LONG).show();
    }
    private void handleAttachment(Uri uri){
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        if(returnCursor != null) {
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            long size = returnCursor.getLong(sizeIndex);
            returnCursor.close();
            if(size > 50 * 1024 * 1024){
                Toast.makeText(this,"File Larger Than 50 MB.",Toast.LENGTH_LONG).show();
            } else {
                String mime = FileUtils.getMimeType(this,uri);
                int messageType = FileUtils.getMessageType(mime);
                processAttachment(uri,userId,name,size,messageType);
            }
        }
    }
    private static void processAttachment(Uri uri,String userId,String fileName,long fileSize, int messageType){
        new processAttachment().execute(uri,userId,fileName,fileSize,messageType);
    }
    private static class processAttachment extends AsyncTask<Object,Void,Void>{
        @Override
        public Void doInBackground(Object... objects){
            Uri uri = (Uri)objects[0];
            String userId = (String)objects[1];
            String fileName = (String)objects[2];
            long fileSize = (long)objects[3];
            int messageType = (int)objects[4];
            long addedAt = System.currentTimeMillis()/1000;
            String messageId = AppPreferences.getString(Keys.userId,"") + userId + addedAt;
            File directory = App.getDirectory();
            File mediaFile = new File(directory, messageId);
            // copy file to local storage as we can't access it later
            try(InputStream inputStream = App.applicationContext.getContentResolver().openInputStream(uri)){
                OutputStream outputStream = new FileOutputStream(mediaFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                String mediaData = "";
                if(messageType == Keys.chatMessageTypePicture){
                    Bitmap originalImage = BitmapFactory.decodeFile(mediaFile.getPath());
                    Bitmap miniImagePreview = ThumbnailUtils.extractThumbnail(originalImage,200,200);
                    Bitmap microImagePreview = ThumbnailUtils.extractThumbnail(miniImagePreview, 30, 30);
                    if(miniImagePreview!= null && microImagePreview != null){
                        FileUtils.saveMediaPreview(miniImagePreview,"preview_"+messageId);
                        mediaData = getBlurredMicroPreviewImageString(microImagePreview);
                    }
                }
                if(messageType == Keys.chatMessageTypeVideo){
                    Bitmap miniVideoPreview = FileUtils.createVideoThumbnail(mediaFile.getPath(),200,200);
                    Bitmap microVideoPreview = ThumbnailUtils.extractThumbnail(miniVideoPreview,30,30);
                    if(miniVideoPreview != null && microVideoPreview != null) {
                        FileUtils.saveMediaPreview(miniVideoPreview, "preview_" + messageId);
                        mediaData = getBlurredMicroPreviewImageString(microVideoPreview);
                    }
                }
                if(messageType == Keys.chatMessageTypeGif){
                    try (FileInputStream fis = new FileInputStream(mediaFile)) {
                        final GifDecoder gifDecoder = new GifDecoder();
                        gifDecoder.read(fis,0);
                        gifDecoder.advance();
                        Bitmap gifFrame = gifDecoder.getNextFrame();
                        if(gifFrame != null){
                            Bitmap miniGifPreview = ThumbnailUtils.extractThumbnail(gifFrame, 200, 200);
                            Bitmap microGifPreview = ThumbnailUtils.extractThumbnail(miniGifPreview,30,30);
                            if(miniGifPreview != null && microGifPreview != null){
                                FileUtils.saveMediaPreview(miniGifPreview,"preview_"+messageId);
                                mediaData = getBlurredMicroPreviewImageString(microGifPreview);
                            }
                        }
                    }
                }
                if(App.chatMediaWithPreview.contains(messageType)){
                    if(TextUtils.isEmpty(mediaData)){
                        mediaFile.delete();
                        handler.post(() -> {showErrorToast("Could Not Process File.");});
                    } else {
                        ChatMessage chatMessage = new ChatMessage(messageId,
                                userId,
                                true,
                                "",
                                addedAt,
                                true,
                                messageType,
                                fileName,
                                fileSize,
                                mediaData,
                                Keys.mediaNotUploaded,
                                Keys.mediaStatusNA);
                        App.insertMessage(chatMessage);
                        Request.getS3ParamsForMedia(messageId);
                    }
                } else {
                    ChatMessage chatMessage = new ChatMessage(messageId,
                            userId,
                            true,
                            "",
                            addedAt,
                            true,
                            messageType,
                            fileName,
                            fileSize,
                            "",
                            Keys.mediaNotUploaded,
                            Keys.mediaStatusNA);
                    App.insertMessage(chatMessage);
                    Request.getS3ParamsForMedia(messageId);
                }
            } catch (Exception e){
                AppLog.e(TAG,"Exception While Copying File:"+e.toString());
                if(mediaFile.exists())mediaFile.delete();
            }
            return null;
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(JSONObject message) throws JSONException {
        int messageType = message.getInt(Keys.messageType);
        switch (messageType) {
            case 27:
                int rs27 = message.getInt(Keys.responseStatus);
                if(rs27 == 1){
                    long lAASeconds = message.getLong(Keys.lastSeenAt);
                    String lastAvailableAt = TimeUtils.toReadable(lAASeconds*1000);
                    _lastAvailable.setVisibility(View.VISIBLE);
                    _lastAvailable.setText("Last Seen : "+lastAvailableAt);
                }
                break;
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(EventUploadProgress event){
        String messageId = event.uploadId;
        int percent = event.percent;
        // TODO Show Progress On Media Message
    }
    // ----------- Chat Item Click Handlers --------
    private void handleLongClick(String messageId){new handleLongClick().execute(messageId);}
    private static class handleLongClick extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String messageId = strings[0];
            List<ChatMessage> chatMessages = App.daoChatMessage.getMessage(messageId);
            if(chatMessages.size() > 0){
                ChatMessage chatMessage = chatMessages.get(0);
                AppLog.e(TAG,"Show Long Click Option For:"+chatMessage.getMessageId());
            }
            return null;
        }
    }
    private void handlePreviewClick(String messageId){new handlePreviewClick().execute(messageId);}
    private static class handlePreviewClick extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String messageId = strings[0];
            List<ChatMessage> chatMessages = App.daoChatMessage.getMessage(messageId);
            if(chatMessages.size() > 0){
                ChatMessage chatMessage = chatMessages.get(0);
                if(chatMessage.getMediaDownloadStatus() < Keys.mediaDownloaded || chatMessage.getMediaUploadStatus() < Keys.mediaUploaded){
                    AppLog.e(TAG,"Preview Clicked But Not Downloaded/Uploaded");
                } else {
                    File mediaFile = FileUtils.getMediaFile(messageId);
                    if(mediaFile.exists()){
                        FileUtils.openFile(mediaFile,chatMessage.getType());
                    } else {
                        handler.post(() -> {showErrorToast("File Does Not Exist");});
                    }
                }
            }
            return null;
        }
    }
    private void handleTransferClick(String messageId){new handleTransferClick().execute(messageId);}
    private static class handleTransferClick extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String messageId = strings[0];
            List<ChatMessage> chatMessages = App.daoChatMessage.getMessage(messageId);
            if(chatMessages.size() > 0){
                ChatMessage chatMessage = chatMessages.get(0);
                if(chatMessage.isMine()){
                    if(chatMessage.getMediaUploadStatus() == Keys.mediaNotUploaded) {
                        Request.getS3ParamsForMedia(messageId);
                    }
                } else {
                   if(chatMessage.getMediaDownloadStatus() == Keys.mediaNotDownloaded){
                       App.downloadMedia(chatMessage);
                   }
                }
            }
            return null;
        }
    }
    private class ChatItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String messageId = v.getContentDescription().toString();
            switch (v.getId()){
                case R.id.transfer_progress:
                    handleTransferClick(messageId);
                    break;
                case R.id.preview:
                    handlePreviewClick(messageId);
                    break;
            }
        }
    }
    private class ChatItemLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()){
                case R.id.preview:
                    String messageId = v.getContentDescription().toString();
                    handleLongClick(messageId);
                    break;
            }
            return true;
        }
    }

    // ------------ To Blur Micro Preview Images ------
    public static Bitmap applyGaussianBlur(Bitmap src) {
        double[][] GaussianBlurConfig = new double[][] {
                { 1, 2, 1 },
                { 2, 4, 2 },
                { 1, 2, 1 }
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(GaussianBlurConfig);
        convMatrix.Factor = 17;
        convMatrix.Offset = 0;
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }
    //---------- Convert Micro Bitmap To Base64 String ------
    private static String getBlurredMicroPreviewImageString(Bitmap microPreview){
        Bitmap blurredMicroPreview = applyGaussianBlur(microPreview);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        blurredMicroPreview.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
