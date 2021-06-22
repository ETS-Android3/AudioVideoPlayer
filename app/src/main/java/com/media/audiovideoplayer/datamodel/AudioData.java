package com.media.audiovideoplayer.datamodel;

public class AudioData {

    private int index;
    private String albumArt;
    private String musicTitle;
    private String artist;
    private String fileUrl;
    private long duration;

    public AudioData(int index, String musicTitle, String artist, String fileUrl, long duration,String albumArt) {
        this.index = index;
        this.musicTitle = musicTitle;
        this.artist = artist;
        this.fileUrl = fileUrl;
        this.duration = duration;
        this.albumArt=albumArt;
    }

    public long getDuration() {
        return duration;
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

    public String getAlbumArt() {
        return albumArt;
    }

}
