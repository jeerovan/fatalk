package com.kaarss.fatalk;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ChatMessageDao {
    @Insert(onConflict = REPLACE)
    void insertMessage(ChatMessage message);

    @Query("DELETE FROM messages WHERE messageId = :mid")
    void deleteMessage(String mid);

    @Query("DELETE FROM messages")
    void clearTable();

    @Query("SELECT COUNT(*) FROM messages WHERE messageId = :id AND type = 1")
    int getDateEntry(String id);

    @Query("SELECT * FROM messages WHERE userId = :userId ORDER BY addedAt DESC")
    LiveData<List<ChatMessage>> getMessages(String userId);

    @Query("SELECT COUNT(*) FROM messages WHERE read = 0")
    LiveData<Integer> getUnread();

    @Query("UPDATE messages SET mediaUploadStatus = 0 WHERE mediaUploadStatus = 1")
    void resetMediaUploadsInProgress();
    @Query("UPDATE messages SET mediaDownloadStatus = 0 WHERE mediaDownloadStatus = 1")
    void resetMediaDownloadsInProgress();

    @Query("UPDATE messages SET mediaUploadStatus = 1 WHERE messageId = :id AND mediaUploadStatus = 0")
    void setMediaUploading(String id);
    @Query("UPDATE messages SET mediaUploadStatus = 0 WHERE messageId = :id AND mediaUploadStatus = 1")
    void setMediaUploadFailed(String id);
    @Query("UPDATE messages SET mediaUploadStatus = 2 WHERE messageId = :id AND mediaUploadStatus = 1")
    void setMediaUploaded(String id);

    @Query("UPDATE messages SET mediaDownloadStatus = 1 WHERE messageId = :id AND mediaDownloadStatus = 0")
    void setMediaDownloading(String id);
    @Query("UPDATE messages SET mediaDownloadStatus = 0 WHERE messageId = :id AND mediaDownloadStatus = 1")
    void setMediaDownloadFailed(String id);
    @Query("UPDATE messages SET mediaDownloadStatus = 2 WHERE messageId = :id AND mediaDownloadStatus = 1")
    void setMediaDownloaded(String id);

    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    List<ChatMessage> getMessage(String messageId);

    @Query("SELECT * FROM messages WHERE mine = 1 AND mediaUploadStatus = 0 ORDER BY addedAt ASC LIMIT 1")
    List<ChatMessage> getMediaMessagesToUpload();

    @Query("UPDATE messages SET sentAt = :at WHERE messageId = :mid AND sentAt = 0")
    void setChatSentAt(String mid, long at);

    @Query("UPDATE messages SET deliveredAt = :deliveredAt WHERE messageId = :mid AND deliveredAt = 0")
    void setChatDeliveredAt(String mid, long deliveredAt);

    @Query("UPDATE messages SET readByAt = :readByAt WHERE messageId = :mid AND readByAt = 0")
    void setChatReadByAt(String mid, long readByAt);

    @Query("UPDATE messages SET read = 1 where userId = :uid AND read = 0")
    void setMessagesRead(String uid);

    @Query("SELECT COUNT(*) FROM messages WHERE userId = :userId")
    int getMessageCount(String userId);

    @Query("SELECT userId,name,age,gender,country,bio,message,messageType,unread"
            + " FROM (SELECT profiles.userId AS userId,"
            + "userName AS name,"
            + "userAge AS age,"
            + "userGender AS gender,"
            + "country AS country,"
            + "bio AS bio,"
            + "achats.message AS message,"
            + "achats.type AS messageType,"
            + "achats.count AS unread"
            + " FROM profiles"
            + " LEFT OUTER JOIN (SELECT count,unread.userId,message,type,addedAt FROM "
            +                           "(SELECT COUNT(*) AS count,userId"
            +                           " FROM messages"
            +                           " WHERE read = 0 GROUP BY userId) AS unread"
            +                  " LEFT OUTER JOIN (SELECT userId,message,type,addedAt"
            +                                   " FROM messages WHERE type != 0"
            +                                   " GROUP BY userId ORDER BY addedAt) AS chats"
            +                  " ON chats.userId = unread.userId"
            +                  " WHERE unread.count > 0 ORDER BY addedAt DESC) AS achats"
            + " ON achats.userId = profiles.userId"
            + " ORDER BY addedAt DESC,profiles.bioChangedAt DESC)"
            )
    LiveData<List<ChatProfile>> getChatProfiles();
}
