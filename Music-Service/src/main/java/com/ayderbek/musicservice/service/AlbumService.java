package com.ayderbek.musicservice.service;

import com.amazonaws.services.alexaforbusiness.model.NotFoundException;
import com.amazonaws.util.IOUtils;
import com.ayderbek.common.AlbumDTO;
import com.ayderbek.musicservice.Request.AlbumRequest;
import com.ayderbek.musicservice.S3.S3Config;
import com.ayderbek.musicservice.S3.S3Service;
import com.ayderbek.musicservice.domain.Album;
import com.ayderbek.musicservice.domain.Artist;
import com.ayderbek.musicservice.exception.ResourceNotFoundException;
import com.ayderbek.musicservice.repository.AlbumRepository;
import com.ayderbek.musicservice.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final S3Service s3Service;
    private final S3Config s3Buckets;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    public Album getById(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new NotFoundException("album not found with id " + albumId));
    }


    // Create a new album
    public AlbumDTO createAlbum(AlbumRequest albumRequest) {
        // Convert the AlbumRequest to an Album entity
        Album album = convertToAlbum(albumRequest);

        // Validate and set the artist for the album
        validateAndSetArtist(album, albumRequest.getArtistId());

        // Save the album entity in the database
        Album savedAlbum = albumRepository.save(album);

        // Convert the saved album to an AlbumDTO and return
        return convertToAlbumDTO(savedAlbum);
    }

    // Read an album by ID
    public AlbumDTO getAlbumById(Long id) {
        // Retrieve the album entity by ID from the database
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album with ID [%s] not found".formatted(id)));

        // Convert the album entity to an AlbumDTO and return
        return convertToAlbumDTO(album);
    }

    // Update an existing album
    public AlbumDTO updateAlbum(Long id, AlbumRequest albumRequest) {
        // Retrieve the existing album entity by ID from the database
        Album existingAlbum = albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album with ID [%s] not found".formatted(id)));

        // Update the existing album entity with the data from the AlbumRequest
        updateAlbumFromRequest(existingAlbum, albumRequest);

        // Validate and set the artist for the album
        validateAndSetArtist(existingAlbum, albumRequest.getArtistId());

        // Save the updated album entity in the database
        Album updatedAlbum = albumRepository.save(existingAlbum);

        // Convert the updated album to an AlbumDTO and return
        return convertToAlbumDTO(updatedAlbum);
    }

    // Delete an album by ID
    public void deleteAlbum(Long id) {
        // Check if the album exists
        if (!albumRepository.existsById(id)) {
            throw new ResourceNotFoundException("Album with ID [%s] not found".formatted(id));
        }

        // Delete the album entity from the database
        albumRepository.deleteById(id);
    }

    // Helper method to convert AlbumRequest to Album entity
    private Album convertToAlbum(AlbumRequest albumRequest) {
        Album album = new Album();
        album.setTitle(albumRequest.getTitle());
        album.setYear(albumRequest.getYear());
        album.setGenre(albumRequest.getGenre());
        album.setCoverArt(albumRequest.getCoverArt());
        return album;
    }

    // Helper method to update Album entity with AlbumRequest data
    private void updateAlbumFromRequest(Album album, AlbumRequest albumRequest) {
        if (albumRequest.getTitle() != null) {
            album.setTitle(albumRequest.getTitle());
        }
        if (albumRequest.getYear() != null) {
            album.setYear(albumRequest.getYear());
        }
        if (albumRequest.getGenre() != null) {
            album.setGenre(albumRequest.getGenre());
        }
        if (albumRequest.getCoverArt() != null) {
            album.setCoverArt(albumRequest.getCoverArt());
        }
    }

    // Helper method to validate and set the artist for the album
    private void validateAndSetArtist(Album album, Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with ID [%s] not found".formatted(artistId)));
        album.setArtist(artist);
    }

    // Helper method to convert Album entity to AlbumDTO
    private AlbumDTO convertToAlbumDTO(Album album) {
        AlbumDTO albumDTO = new AlbumDTO();
        albumDTO.setId(album.getId());
        albumDTO.setTitle(album.getTitle());
        albumDTO.setArtistId(album.getArtist().getId());
        albumDTO.setYear(album.getYear());
        albumDTO.setGenre(album.getGenre());
        albumDTO.setCoverArt(album.getCoverArt());
        return albumDTO;
    }

    public void uploadSongCoverArtImage(Long albumId, MultipartFile file) {
//        checkIfSongExistsOrThrow(songId);
        String coverArtId = UUID.randomUUID().toString();
        try {
            byte[] coverArtBytes = file.getBytes();
            String s3Key = "CoverArt-images/%s/%s".formatted(albumId, coverArtId);
            s3Service.putObject(
                    s3Buckets.getSpotify(),
                    s3Key,
                    file.getInputStream(), coverArtBytes
            );

            String cloudFrontUrl = "https://%s/%s".formatted(cloudFrontDomain, s3Key);

            Album album = getById(albumId);
            album.setCoverArt(cloudFrontUrl);
            albumRepository.save(album);
        } catch (IOException e) {
            throw new RuntimeException("failed to upload coverArt image", e);
        }
    }

    public byte[] getSongCoverArtImage(Long albumId) {
        Album album = getById(albumId);
        String cloudFrontUrl = album.getCoverArt();

        try {
            // Retrieve the image directly from CloudFront
            URL url = new URL(cloudFrontUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            byte[] coverArtBytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            return coverArtBytes;
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve coverArt image from CloudFront", e);
        }
    }
}
