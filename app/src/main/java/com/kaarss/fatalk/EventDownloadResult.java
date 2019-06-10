package com.kaarss.fatalk;

public class EventDownloadResult {
    public String downloadId;
    public int downloadType;
    public boolean success;
    EventDownloadResult(String downloadId, int downloadType, boolean success){
        this.downloadId = downloadId;
        this.downloadType = downloadType;
        this.success = success;
    }
}
