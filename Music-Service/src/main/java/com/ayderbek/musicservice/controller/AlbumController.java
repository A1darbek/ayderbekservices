package com.ayderbek.musicservice.controller;

import com.ayderbek.common.AlbumDTO;
import com.ayderbek.musicservice.Request.AlbumRequest;
import com.ayderbek.musicservice.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;

    @PostMapping(
            value = "{albumId}/coverArt-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void uploadCoverArtImage(
            @PathVariable("albumId") Long albumId,
            @RequestParam("file") MultipartFile file) {
        albumService.uploadSongCoverArtImage(albumId, file);
    }

    @GetMapping(
            value = "{albumId}/coverArt-image",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] getCoverArtImage(
            @PathVariable("albumId") Long albumId) {
        return albumService.getSongCoverArtImage(albumId);
    }

    @PostMapping
    public ResponseEntity<AlbumDTO> createAlbum(@RequestBody AlbumRequest albumRequest) {
        AlbumDTO createdAlbum = albumService.createAlbum(albumRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAlbum);
    }

    // Read an album by ID
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDTO> getAlbumById(@PathVariable Long id) {
        AlbumDTO album = albumService.getAlbumById(id);
        return ResponseEntity.ok(album);
    }

    // Update an existing album
    @PutMapping("/{id}")
    public ResponseEntity<AlbumDTO> updateAlbum(@PathVariable Long id, @RequestBody AlbumRequest albumRequest) {
        AlbumDTO updatedAlbum = albumService.updateAlbum(id, albumRequest);
        return ResponseEntity.ok(updatedAlbum);
    }

    // Delete an album by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }
}
