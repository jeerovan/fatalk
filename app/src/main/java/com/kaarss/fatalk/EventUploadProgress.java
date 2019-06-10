package com.kaarss.fatalk;

public class EventUploadProgress {
    public String uploadId;
    public int uploadType;
    public int percent;
    EventUploadProgress(String uploadId,int uploadType,int percent) {
        this.uploadId = uploadId;
        this.uploadType = uploadType;
        this.percent = percent;
    }
}
