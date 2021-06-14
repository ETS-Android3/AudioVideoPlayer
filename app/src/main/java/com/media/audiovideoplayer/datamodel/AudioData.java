package com.media.audiovideoplayer.datamodel;

public class AudioData {

    private int index;
    private String musicTitle;
    private String artist;
    private String fileUrl;

    public long getDuration() {
        return duration;
    }

    private long duration;

    public AudioData(int index, String musicTitle, String artist, String fileUrl, long duration) {
        this.index = index;
        this.musicTitle = musicTitle;
        this.artist = artist;
        this.fileUrl = fileUrl;
        this.duration = duration;
    }

    public int getIndex() {
        return index;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public String getArtist() {
        return artist;
    }

    public String getFileUrl() {
        return fileUrl;
    }

}
