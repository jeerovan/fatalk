package com.kaarss.fatalk;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface TaskDao {
    @Insert(onConflict = REPLACE)
    void insertTask(Task task);

    @Query("DELETE FROM tasks WHERE taskId = :taskId")
    void deleteTask(String taskId);

    @Query("DELETE FROM tasks")
    void clearTable();

    @Query("SELECT * FROM tasks")
    List<Task> getTasks();
}
