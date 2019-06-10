package com.kaarss.fatalk;

public class EventDownloadProgress {
    public String downloadId;
    public int downloadType;
    public int percent;
    EventDownloadProgress(String downloadId, int downloadType, int percent) {
        this.downloadId = downloadId;
        this.downloadType = downloadType;
        this.percent = percent;
    }
}
