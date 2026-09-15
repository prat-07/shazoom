package com.pratham.backend.service;

import com.pratham.backend.engine.algorithm.FingerprintGenerator;
import com.pratham.backend.engine.model.Couple;
import com.pratham.backend.entity.FingerprintRecord;
import com.pratham.backend.entity.Song;
import com.pratham.backend.repository.FingerprintRepository;
import com.pratham.backend.repository.SongRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SongIngestionService {

    private final SongRepository songRepository;
    private final FingerprintRepository fingerprintRepository;

    @Autowired
    public SongIngestionService(
            SongRepository songRepository,
            FingerprintRepository fingerprintRepository
    ) {
        this.songRepository = songRepository;
        this.fingerprintRepository = fingerprintRepository;
    }

    public static class SongAlreadyExistsException extends RuntimeException{
        public SongAlreadyExistsException(String message){ super(message); }
    }

    @Transactional
    public Song ingest(MultipartFile file,
                       String title,
                       String artist) throws Exception{

        String key = buildKey(title, artist);

        Optional<Song> existing = songRepository.findByKey(key);
        if(existing.isPresent())
            throw new SongAlreadyExistsException(
                    "Song already exists: " + title + " - " + artist
            );

        Song song = new Song();
        song.setTitle(title);
        song.setArtist(artist);
        song.setKey(key);
        song = songRepository.save(song);

        Map<Integer, Couple> fingerprint;
        try(InputStream in = new BufferedInputStream(file.getInputStream())){
            fingerprint = FingerprintGenerator.generateFingerprintForAudio(song.getId(), in);
        }

        List<FingerprintRecord> records = new ArrayList<>();
        for(var entry : fingerprint.entrySet()){
            Integer address = entry.getKey();
            Couple couple = entry.getValue();
            records.add(new FingerprintRecord(address, couple.getSongId(), couple.getAnchorTimeMs()));
        }

        fingerprintRepository.saveAll(records);

        return song;
    }

    private String buildKey(String title, String artist){
        return (title.trim() + "---" + artist.trim()).toLowerCase();
    }
}
