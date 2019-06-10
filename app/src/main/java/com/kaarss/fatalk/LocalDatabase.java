package com.kaarss.fatalk;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {UserProfile.class,Task.class,ChatMessage.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {

    public abstract UserProfileDao userProfileDao();
    public abstract TaskDao taskDao();
    public abstract ChatMessageDao chatMessageDao();

    private static LocalDatabase INSTANCE;

    static LocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(App.applicationContext,
                            LocalDatabase.class, "database")
                            .build();

                }
            }
        }
        return INSTANCE;
    }
}