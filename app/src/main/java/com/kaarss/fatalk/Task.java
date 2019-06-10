package com.kaarss.fatalk;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "tasks")
public class Task {
    @PrimaryKey
    @NonNull
    private String taskId;
    private String data;

    public Task(@NonNull String taskId, String data) {
        this.taskId = taskId;
        this.data = data;
    }

    @NonNull
    public String getTaskId() {
        return taskId;
    }

    public String getData() {
        return data;
    }

}
