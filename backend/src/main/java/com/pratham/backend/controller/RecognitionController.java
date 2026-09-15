package com.pratham.backend.controller;

import com.pratham.backend.entity.Song;
import com.pratham.backend.service.RecognitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Optional;

@RestController
@RequestMapping("/api/recognize")
public class RecognitionController {

    private final RecognitionService recognitionService;

    public RecognitionController(RecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    @PostMapping
    public ResponseEntity<?> recognize(@RequestParam MultipartFile file) {

        try {
            Optional<Song> match = recognitionService.recognize(file);

            return match
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("No match found"));

        } catch (UnsupportedAudioFileException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unsupported or invalid audio file: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process recording: " + e.getMessage());
        }
    }
}