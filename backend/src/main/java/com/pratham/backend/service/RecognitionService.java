package com.pratham.backend.service;

import com.pratham.backend.engine.algorithm.FingerprintGenerator;
import com.pratham.backend.engine.algorithm.Matcher;
import com.pratham.backend.engine.model.Couple;
import com.pratham.backend.entity.FingerprintRecord;
import com.pratham.backend.entity.Song;
import com.pratham.backend.repository.FingerprintRepository;
import com.pratham.backend.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class RecognitionService {

    private static final double MIN_MATCH_SCORE = 5.0;
    private final SongRepository songRepository;
    private final FingerprintRepository fingerprintRepository;

    @Autowired
    public RecognitionService(
            SongRepository songRepository,
            FingerprintRepository fingerprintRepository
    ){
        this.songRepository = songRepository;
        this.fingerprintRepository = fingerprintRepository;
    }

    public Optional<Song> recognize(MultipartFile file) throws Exception{

        Map<Integer, Couple> fingerprint;
        try(InputStream in = new BufferedInputStream(file.getInputStream())){
            fingerprint = FingerprintGenerator.generateFingerprintForAudio(0L, in);
        }

        if(fingerprint.isEmpty())   return Optional.empty();

        //discard dummy songId from the fingerprint generated of the audio.
        //this specific data structure needed by the Matcher class' findBestMatch() func
        Map<Integer, Long> sampleFingerprint = new HashMap<>();
        for(var entry : fingerprint.entrySet())
            sampleFingerprint.put(
                    entry.getKey(),
                    entry.getValue().getAnchorTimeMs()
            );

        List<Integer> addresses = new ArrayList<>(sampleFingerprint.keySet());  //generated addresses of audio
        List<FingerprintRecord> dbMatches = fingerprintRepository.findByAddressIn(addresses);   //all addresses in the db matching with generated

        Optional<Matcher.MatchResult> result =
                Matcher.findBestMatch(sampleFingerprint, dbMatches);

        if(result.isEmpty() || result.get().score() < MIN_MATCH_SCORE)
            return Optional.empty();

        return songRepository.findById(result.get().songId());
    }

}
