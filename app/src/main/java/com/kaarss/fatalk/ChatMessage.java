package com.kaarss.fatalk;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "messages")
public class ChatMessage {
    @PrimaryKey
    @NonNull
    private String messageId;
    private String userId; // chatWithUserId
    private boolean mine;
    private String message; // ChatMessage/ MediaMessage
    private long addedAt; // Local Timestamp, To Sort For Display
    private long sentAt = 0; // Server Timestamp To Display SentAt
    private long deliveredAt = 0;
    private long readByAt = 0; // By Other User
    private boolean read; // (0/1)read by me not the other user
    private int type; // chatMessage Types
    private String mediaName;
    private long mediaSize;
    private String mediaData;
    private int mediaUploadStatus; // 0:NotUploaded;1:Uploading;2:Uploaded
    private int mediaDownloadStatus; // 0:NotDownloaded;1:Downloading;3:Downloaded

    public ChatMessage(@NonNull String messageId, String userId, boolean mine, String message, long addedAt, boolean read, int type, String mediaName, long mediaSize, String mediaData, int mediaUploadStatus,int mediaDownloadStatus) {
        this.messageId = messageId;
        this.userId = userId;
        this.mine = mine;
        this.message = message;
        this.addedAt = addedAt;
        this.read = read;
        this.type = type;
        this.mediaName = mediaName;
        this.mediaSize = mediaSize;
        this.mediaData = mediaData;
        this.mediaUploadStatus = mediaUploadStatus;
        this.mediaDownloadStatus = mediaDownloadStatus;
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }
    public String getUserId() {
        return userId;
    }
    public boolean isMine() {
        return mine;
    }
    public String getMessage() {
        return message;
    }
    public long getAddedAt() {
        return addedAt;
    }
    public boolean isSent() {
        return sentAt > 0;
    }
    public long getSentAt(){return sentAt;}
    public boolean isDelivered() { return deliveredAt > 0;}
    public long getDeliveredAt(){return deliveredAt;}
    public boolean isReadBy(){return readByAt > 0; }
    public long getReadByAt(){return readByAt;}
    public boolean isRead() {
        return read;
    }
    public int getType() {
        return type;
    }
    public String getMediaName() { return mediaName; }
    public long getMediaSize() { return mediaSize; }
    public String getMediaData() { return mediaData; }
    public int getMediaUploadStatus() { return mediaUploadStatus; }
    public int getMediaDownloadStatus(){return mediaDownloadStatus;}

    public void setSentAt(long sentAt){
        this.sentAt = sentAt;
    }
    public void setDeliveredAt(long deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    public void setReadByAt(long readByAt) {
        this.readByAt = readByAt;
    }
    public void setMediaUploadStatus(int mediaUploadStatus) { this.mediaUploadStatus = mediaUploadStatus; }
    public void setMediaDownloadStatus(int mediaDownloadStatus){this.mediaDownloadStatus = mediaDownloadStatus;}
}
