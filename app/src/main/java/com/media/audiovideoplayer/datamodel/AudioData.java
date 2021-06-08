package com.media.audiovideoplayer.datamodel;

public class AudioData {

    private int index;
    private String musicTitle;
    private String artist;
    private String fileUrl;
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

    public void setIndex(int index) {
        this.index = index;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }


}
