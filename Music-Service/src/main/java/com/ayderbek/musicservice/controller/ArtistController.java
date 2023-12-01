package com.ayderbek.musicservice.controller;

import com.ayderbek.common.ArtistDTO;
import com.ayderbek.musicservice.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/artists")
@RequiredArgsConstructor
public class ArtistController {
    private final ArtistService artistService;

    @PostMapping(
            value = "{artistId}/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void uploadProfileImage(
            @PathVariable("artistId") Long artistId,
            @RequestParam("file") MultipartFile file) {
        artistService.uploadProfileImage(artistId, file);
    }

    @GetMapping(
            value = "{artistId}/profile-image",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public byte[] getProfileImage(
            @PathVariable("artistId") Long artistId) {
        return artistService.getProfileImage(artistId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable Long id) {
        ArtistDTO artist = artistService.getArtistById(id);
        return ResponseEntity.ok(artist);
    }
}
