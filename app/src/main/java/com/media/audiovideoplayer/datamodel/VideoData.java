package com.media.audiovideoplayer.datamodel;

public class VideoData {

    private String title;
    private String displayName;
    private String url;
    private long duration;


    public VideoData(String title, String displayName, String url,long duration) {
        this.title = title;
        this.displayName = displayName;
        this.url = url;
        this.duration=duration;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public long getDuration() {
        return duration;
    }

}
