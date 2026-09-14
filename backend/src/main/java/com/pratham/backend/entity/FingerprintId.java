package com.pratham.backend.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Key class to represent the composite primary key of 'FingerprintRecord'
 * JPA requires composite key classes to be serializable
 */

public class FingerprintId implements Serializable {

    private Integer address;
    private Long songId;
    private Long anchorTimeMs;

    public FingerprintId(){
    }

    public FingerprintId(
            Integer address,
            Long songId,
            Long anchorTimeMs
    ){
        this.address = address;
        this.songId = songId;
        this.anchorTimeMs = anchorTimeMs;
    }

    @Override
    public boolean equals(Object o){
        if(this == o)   return true;
        if(!(o instanceof  FingerprintId that)) return false;

        return Objects.equals(address, that.address)
                && Objects.equals(songId, that.songId)
                && Objects.equals(anchorTimeMs, that.anchorTimeMs);
    }

    @Override
    public int hashCode(){
        return Objects.hash(address, songId, anchorTimeMs);
    }
}

