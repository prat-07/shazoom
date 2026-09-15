package com.pratham.backend.controller;

import com.pratham.backend.entity.Song;
import com.pratham.backend.service.SongIngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongIngestionService songIngestionService;

    public SongController(SongIngestionService songIngestionService) {
        this.songIngestionService = songIngestionService;
    }

    @PostMapping
    public ResponseEntity<?> upload(
            @RequestParam MultipartFile file,
            @RequestParam String title,
            @RequestParam String artist) {

        try {
            Song saved = songIngestionService.ingest(file, title, artist);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (SongIngestionService.SongAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (UnsupportedAudioFileException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unsupported or invalid audio file: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process song: " + e.getMessage());
        }
    }
}