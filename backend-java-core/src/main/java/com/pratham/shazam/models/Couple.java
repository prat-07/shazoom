package com.pratham.shazam.models;

public class Couple {

    private final long songId;
    private final long anchorTimeMs;

    public Couple(long songId, long anchorTimeMs) {
        this.songId = songId;
        this.anchorTimeMs = anchorTimeMs;
    }

    public long getSongId() {
        return songId;
    }

    public long getAnchorTimeMs() {
        return anchorTimeMs;
    }

    @Override
    public String toString() {
        return "Couple{" +
                "songId=" + songId +
                ", anchorTimeMs=" + anchorTimeMs +
                '}';
    }
}
