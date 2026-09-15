package com.pratham.backend.repository;

import com.pratham.backend.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {

    Optional<Song> findByKey(String key);
}
