package com.ayderbek.musicservice.service;

import com.amazonaws.services.alexaforbusiness.model.NotFoundException;
import com.amazonaws.util.IOUtils;
import com.ayderbek.common.ArtistDTO;
import com.ayderbek.musicservice.S3.S3Config;
import com.ayderbek.musicservice.S3.S3Service;
import com.ayderbek.musicservice.domain.Artist;
import com.ayderbek.musicservice.exception.ResourceNotFoundException;
import com.ayderbek.musicservice.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {
    private final S3Service s3Service;
    private final S3Config s3Buckets;
    private final ArtistRepository artistRepository;

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    public Artist getById(Long artistId) {
        return artistRepository.findById(artistId)
                .orElseThrow(() -> new NotFoundException("artist not found with id " + artistId));
    }


    public ArtistDTO getArtistById(Long id) {
        // Retrieve the artist entity by ID from the database
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with ID [%s] not found".formatted(id)));

        // Convert the artist entity to an ArtistDTO and return
        return convertToArtistDTO(artist);
    }

    // Read all artists
    public List<ArtistDTO> getAllArtists() {
        // Retrieve all artist entities from the database
        List<Artist> artists = artistRepository.findAll();

        // Convert the artist entities to a list of ArtistDTOs and return
        return artists.stream()
                .map(this::convertToArtistDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert Artist entity to ArtistDTO
    private ArtistDTO convertToArtistDTO(Artist artist) {
        ArtistDTO artistDTO = new ArtistDTO();
        artistDTO.setId(artist.getId());
        artistDTO.setName(artist.getName());
        artistDTO.setBio(artist.getBio());
        artistDTO.setProfilePicture(artist.getProfilePicture());
        return artistDTO;
    }

    public void uploadProfileImage(Long artistId, MultipartFile file) {
//        checkIfSongExistsOrThrow(songId);
        String profilePictureId = UUID.randomUUID().toString();
        try {
            byte[] profilePictureBytes = file.getBytes();
            String s3Key = "profile-images/%s/%s".formatted(artistId, profilePictureId);
            s3Service.putObject(
                    s3Buckets.getSpotify(),
                    s3Key,
                    file.getInputStream(), profilePictureBytes
            );

            String cloudFrontUrl = "https://%s/%s".formatted(cloudFrontDomain, s3Key);

            Artist artist = getById(artistId);
            artist.setProfilePicture(cloudFrontUrl);
            artistRepository.save(artist);
        } catch (IOException e) {
            throw new RuntimeException("failed to upload coverArt image", e);
        }
    }

    public byte[] getProfileImage(Long artistId) {
        Artist artist = getById(artistId);
        String cloudFrontUrl = artist.getProfilePicture();

        try {
            // Retrieve the image directly from CloudFront
            URL url = new URL(cloudFrontUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            byte[] profilePictureBytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            return profilePictureBytes;
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve profilePicture image from CloudFront", e);
        }
    }
}
