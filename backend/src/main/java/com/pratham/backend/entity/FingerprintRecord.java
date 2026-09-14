package com.pratham.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fingerprints")
@IdClass(FingerprintId.class)
public class FingerprintRecord {

    @Id
    @Column(nullable = false)
    private Integer address;

    @Id
    @Column(name = "song_id", nullable = false)
    private Long songId;

    @Id
    @Column(name = "anchor_time_ms", nullable = false)
    private Long anchorTimeMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "song_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false
    )
    private Song song;

    public FingerprintRecord(){
    }

    public FingerprintRecord(
            Integer address,
            Long songId,
            Long anchorTimeMs
    ){
        this.address = address;
        this.songId = songId;
        this.anchorTimeMs = anchorTimeMs;
    }

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }

    public Long getAnchorTimeMs() {
        return anchorTimeMs;
    }

    public void setAnchorTimeMs(Long anchorTimeMs) {
        this.anchorTimeMs = anchorTimeMs;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }
}
