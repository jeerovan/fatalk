package com.kaarss.fatalk;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface UserProfileDao {
    @Insert(onConflict = REPLACE)
    void insertProfile(UserProfile profile);

    @Query("DELETE FROM profiles WHERE userId = :uid")
    void deleteProfile(String uid);

    @Query("DELETE FROM profiles")
    void clearTable();

    @Query("SELECT * FROM profiles WHERE userId = :userId")
    List<UserProfile> getProfile(String userId);

    @Query("SELECT userId || '|' || bioChangedAt FROM profiles")
    List<String> getAllProfiles();

    @Query("UPDATE profiles SET dpVersion = :dpVersion WHERE userId = :userId")
    void setProfileDpVersion(String userId, int dpVersion);

    @Query("UPDATE profiles SET dpVersion = :dpVersion,bio = :bio, bioChangedat = :bioChangedAt WHERE userId = :userId")
    void setProfileDpBio(String userId, int dpVersion, String bio, long bioChangedAt);

    @Query("UPDATE profiles SET interactedAt = :lastContactedAt WHERE userId = :userId")
    void updateLastContactedAt(String userId, long lastContactedAt);

    @Query("DELETE FROM profiles WHERE userId NOT IN (SELECT DISTINCT userId FROM messages)")
    void removeNonChatUsers();
}
