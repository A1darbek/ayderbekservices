package com.ayderbek.musicservice.controller;

import com.ayderbek.common.SongDTO;
import com.ayderbek.musicservice.Request.SongRequest;
import com.ayderbek.musicservice.config.QueryType;
import com.ayderbek.musicservice.exception.ResourceNotFoundException;
import com.ayderbek.musicservice.service.ElasticsearchService;
import com.ayderbek.musicservice.service.SongService;
import com.ayderbek.musicservice.service.SongStreamingService;
import com.ayderbek.musicservice.websocket.SongWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
@Slf4j
public class SongController {
    private final SongService songService;
    private final SongStreamingService songStreamingService;
    private final ElasticsearchService elasticsearchService;
    private final SongWebSocketHandler songWebSocketHandler;

    @GetMapping
    public ResponseEntity<List<SongDTO>> getAllSongs() {
        List<SongDTO> songs = songStreamingService.getAllSongs();
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/wildcard")
    public ResponseEntity<String> searchByWildcard(@RequestParam String query) {
        try {
            String response = elasticsearchService.search(query, QueryType.WILDCARD);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Elasticsearch request failed.");
        }
    }

    @GetMapping("/multi-match")
    public ResponseEntity<String> searchByMultiMatch(@RequestParam String query) {
        try {
            String response = elasticsearchService.search(query, QueryType.MULTI_MATCH);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Elasticsearch request failed.");
        }
    }

    @GetMapping("/fuzzy")
    public ResponseEntity<String> searchByFuzzy(@RequestParam String query) {
        try {
            String response = elasticsearchService.search(query, QueryType.FUZZY);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Elasticsearch request failed.");
        }
    }



    @GetMapping("/{songId}/stream")
    public ResponseEntity<String> getSongStream(@PathVariable Long songId) {
        try {
            String signedUrl = songStreamingService.getSignedCloudFrontUrl(songId);
            return ResponseEntity.ok(signedUrl);
        } catch (Exception e) {
            log.error("Error generating signed URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating signed URL.");
        }
    }


    @GetMapping("/{songId}")
    public ResponseEntity<SongDTO> getSongDetails(@PathVariable Long songId) {
        try {
            SongDTO songWithDetails = songStreamingService.getSongWithDetails(songId);
            return ResponseEntity.ok(songWithDetails);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping(
            value = "{songId}/thumbnail-image",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] getSongThumbnailImage(
            @PathVariable("songId") Long songId) {
        return songService.getSongThumbnailImage(songId);
    }

    @PostMapping
    public ResponseEntity<?> createSong(
            @ModelAttribute SongRequest songRequest,
            @RequestParam("songFile") MultipartFile songFile,
            @RequestParam("imageFile") MultipartFile imageFile
    ) {
        try {
            SongDTO createdSong = songService.createSong(songRequest,imageFile,songFile);

            // Notify connected clients about the new song
            songWebSocketHandler.notifyClientsAboutNewSong(createdSong);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdSong);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create song: " + e.getMessage());
        }
    }

    // Update an existing song
    @PutMapping("/{id}")
    public ResponseEntity<SongDTO> updateSong(@PathVariable Long id, @RequestBody SongRequest songRequest) {
        SongDTO updatedSong = songService.updateSong(id, songRequest);
        return ResponseEntity.ok(updatedSong);
    }

    // Delete a song by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

}
