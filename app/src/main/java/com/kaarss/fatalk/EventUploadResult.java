package com.kaarss.fatalk;

public class EventUploadResult {
    public String uploadId;
    public int uploadType;
    public boolean success;
    EventUploadResult(String uploadId, int uploadType, boolean success){
        this.uploadId = uploadId;
        this.uploadType = uploadType;
        this.success = success;
    }
}
