package com.kaarss.fatalk;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {UserProfile.class,Task.class,ChatMessage.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {

    public abstract DaoUserProfile userProfileDao();
    public abstract DaoTask taskDao();
    public abstract DaoChatMessage chatMessageDao();

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