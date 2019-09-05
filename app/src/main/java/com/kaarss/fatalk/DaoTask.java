package com.kaarss.fatalk;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface DaoTask {
    @Insert(onConflict = REPLACE)
    void insertTask(Task task);

    @Query("DELETE FROM tasks WHERE taskId = :taskId")
    void deleteTask(String taskId);

    @Query("DELETE FROM tasks")
    void clearTable();

    @Query("SELECT * FROM tasks")
    List<Task> getTasks();
}
