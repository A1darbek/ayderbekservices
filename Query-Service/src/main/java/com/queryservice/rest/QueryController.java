package com.queryservice.rest;

import com.ayderbek.common.PlaylistDTO;
import com.ayderbek.common.SongDTO;
import com.queryservice.Denormalization.PlaylistWithSongDetailsDTO;
import com.queryservice.client.MusicServiceFeignClient;
import com.queryservice.client.SocialServiceFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/cqrs-test")
@Slf4j
@RequiredArgsConstructor
public class QueryController {

    private final SocialServiceFeignClient socialServiceFeignClient;
    private final MusicServiceFeignClient musicServiceFeignClient;

    @GetMapping("/playTest/{playlistId}")
    public ResponseEntity<PlaylistDTO> getPlaylistTest(@PathVariable Long playlistId) {
        try {
            PlaylistDTO playlistDTO = socialServiceFeignClient.getPlaylistDetails(playlistId);

            if (playlistDTO != null) {
                log.info("Playlist Details: {}", playlistDTO);
                return ResponseEntity.ok(playlistDTO);
            } else {
                log.error("Failed to retrieve playlist details.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (FeignException.NotFound e) {
            log.error("Playlist not found. Playlist ID: {}", playlistId, e);
            return ResponseEntity.notFound().build();
        } catch (FeignException e) {
            log.error("An error occurred in getPlaylistTest method. Playlist ID: {}", playlistId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/songTest/{songId}")
    public ResponseEntity<SongDTO> getSongTest(@PathVariable Long songId) {
        try {
            SongDTO songDTO = musicServiceFeignClient.getSongDetails(songId).getBody();

            if (songDTO != null) {
                log.info("Song Details: {}", songDTO);
                return ResponseEntity.ok(songDTO);
            } else {
                log.error("Failed to retrieve song details.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (FeignException.NotFound e) {
            log.error("Song not found. Song ID: {}", songId, e);
            return ResponseEntity.notFound().build();
        } catch (FeignException e) {
            log.error("An error occurred in getSongTest method. Song ID: {}", songId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/playlistWithSongDetails/{playlistId}")
    public ResponseEntity<PlaylistWithSongDetailsDTO> getPlaylistWithSongDetails(@PathVariable Long playlistId) {
        try {
            // Fetch playlist details from Social-Service
            PlaylistDTO playlistDTO = socialServiceFeignClient.getPlaylistDetails(playlistId);

            if (playlistDTO != null) {
                // Fetch song details for each song in the playlist from Music-Service
                List<Long> songIds = playlistDTO.getSongIds();
                List<SongDTO> songs = new ArrayList<>();

                for (Long songId : songIds) {
                    ResponseEntity<SongDTO> songResponseEntity = musicServiceFeignClient.getSongDetails(songId);

                    if (songResponseEntity.getStatusCode() == HttpStatus.OK) {
                        SongDTO songDTO = songResponseEntity.getBody();
                        songs.add(songDTO);
                    }
                }

                // Create a DTO that combines playlist and song details
                PlaylistWithSongDetailsDTO playlistWithSongDetailsDTO = new PlaylistWithSongDetailsDTO();
                playlistWithSongDetailsDTO.setId(playlistDTO.getId());
                playlistWithSongDetailsDTO.setTitle(playlistDTO.getTitle());
                playlistWithSongDetailsDTO.setCreatorId(playlistDTO.getCreatorId());
                playlistWithSongDetailsDTO.setCoverArt(playlistDTO.getCoverArt());
                playlistWithSongDetailsDTO.setSongs(songs);

                log.info("Aggregated PlaylistWithSongDetailsDTO: {}", playlistWithSongDetailsDTO);

                return ResponseEntity.ok(playlistWithSongDetailsDTO);
            } else {
                log.error("Failed to retrieve playlist details.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (FeignException.NotFound e) {
            log.error("Playlist not found. Playlist ID: {}", playlistId, e);
            return ResponseEntity.notFound().build();
        } catch (FeignException e) {
            log.error("An error occurred in getPlaylistWithSongDetails method. Playlist ID: {}", playlistId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
