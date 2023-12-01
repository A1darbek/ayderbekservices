package com.ayderbek.socialservice.controller;

import com.ayderbek.common.PlaylistDTO;
import com.ayderbek.socialservice.Request.PlaylistRequest;
import com.ayderbek.socialservice.config.QueryType;
import com.ayderbek.socialservice.service.ElasticsearchService;
import com.ayderbek.socialservice.service.PlayListService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlayListController {
    private final PlayListService playlistService;
    private final ElasticsearchService elasticsearchService;

    @GetMapping("/wildcard")
    public ResponseEntity<String> searchByWildcard(@RequestParam String query) {
        try {
            String response = elasticsearchService.search(query, QueryType.WILDCARD);
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

    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(
            @ModelAttribute PlaylistRequest playlistRequest,
            @RequestParam(value = "coverArtImage", required = false) MultipartFile coverArtImage
    ) {
        try {
            PlaylistDTO createdPlaylist = playlistService.createPlaylist(playlistRequest, coverArtImage);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPlaylist);
        } catch (Exception e) {
            // Handle the exception and return an appropriate response (e.g., 400 Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }



    @GetMapping("/all")
    public ResponseEntity<List<PlaylistDTO>> getAllPlaylists() {
        List<PlaylistDTO> playlists = playlistService.getAllPlaylists();
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/{playlistId}")
    public PlaylistDTO getPlaylistDetails(@PathVariable Long playlistId) {
        try {
            return playlistService.getPlaylistWithDetails(playlistId);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e);
        }
    }
}
