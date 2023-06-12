package io.agora.metachat.example.models.idol;

import androidx.annotation.NonNull;

import io.agora.mediaplayer.Constants;

public class BgmMusic {
    private long songCode;
    private String name;
    private String singer;
    private Constants.MediaPlayerState state;

    public long getSongCode() {
        return songCode;
    }

    public String getName() {
        return name;
    }

    public String getSinger() {
        return singer;
    }

    public Constants.MediaPlayerState getState() {
        return state;
    }

    public void setSongCode(long songCode) {
        this.songCode = songCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public void setState(Constants.MediaPlayerState state) {
        this.state = state;
    }

    @NonNull
    @Override
    public String toString() {
        return "BgmMusic{" +
                "songCode=" + songCode +
                ", name='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", state=" + state +
                '}';
    }
}
