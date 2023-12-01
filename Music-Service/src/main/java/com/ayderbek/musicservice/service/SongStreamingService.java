package com.ayderbek.musicservice.service;

import com.amazonaws.services.alexaforbusiness.model.NotFoundException;
import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import com.ayderbek.common.AlbumDTO;
import com.ayderbek.common.ArtistDTO;
import com.ayderbek.common.SongDTO;
import com.ayderbek.musicservice.domain.Song;
import com.ayderbek.musicservice.exception.ResourceNotFoundException;
import com.ayderbek.musicservice.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongStreamingService {
    private final SongService songService;
    private final SongRepository songRepository;
    private final ModelMapper modelMapper;

    @Value("${cloudfront.privateKeyPath}")
    private String privateKeyPath;

    public String getSignedCloudFrontUrl(Long songId) throws Exception {
        // Retrieve the video
        Song song = songService.getById(songId);

        // Get the S3 object key from the video
        String s3ObjectKey = song.getKey();

        // Key pair ID from the CloudFront console
        String keyPairId = "APKA43UPJH7H766E6EMC";

        // Set up the expiration date of the signed URL (valid for 24 hours)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 24);
        Date expirationDate = cal.getTime();

        // Load the private key file
        File privateKeyFile = new File(privateKeyPath);
        // The domain of your CloudFront distribution
        String distributionDomain = "d2iujsfqj92c6r.cloudfront.net";

        // Generate the signed URL
        String signedUrlCannedPolicy = CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
                SignerUtils.Protocol.http, distributionDomain, privateKeyFile, s3ObjectKey, keyPairId, expirationDate);

        // Return the signed URL
        return signedUrlCannedPolicy;
    }

    public String getSongThumbnailUrl(Long songId) {
        Song song = getById(songId);
        return song.getThumbnail();
    }


    public Song getById(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() -> new NotFoundException("Song not found with id " + songId));
    }

    public SongDTO getSongWithDetails(Long songId) throws Exception {
        // Retrieve the song entity by ID from the database
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song with ID [%s] not found".formatted(songId)));

        // Get the signed CloudFront URL for the song
        String signedUrl = getSignedCloudFrontUrl(songId);

        // Get the thumbnail image as byte[]
        String thumbnailBytes = getSongThumbnailUrl(songId);

        // Convert the song entity to a SongDTO
        SongDTO songDTO = convertToSongDTO(song);

        // Set the signed URL and thumbnail in the SongDTO
        songDTO.setSignedUrl(signedUrl);
        songDTO.setThumbnail(thumbnailBytes);

        // Return the SongDTO with all details
        return songDTO;
    }

    public List<SongDTO> getAllSongs() {
        List<Song> songs = songRepository.findAll();

        return songs.stream()
                .map(this::mapSongToDTO)
                .collect(Collectors.toList());
    }

    private SongDTO mapSongToDTO(Song song) {
        SongDTO songDTO = modelMapper.map(song, SongDTO.class);
        try {
            // Set the signed CloudFront URL
            songDTO.setSignedUrl(getSignedCloudFrontUrl(song.getId()));
        } catch (Exception e) {
            // Handle the exception, e.g., log it or set a default value for signedUrl
            songDTO.setSignedUrl("Error generating signed URL");
        }
        return songDTO;
    }

    public SongDTO convertToSongDTO(Song song) {
        SongDTO songDTO = modelMapper.map(song, SongDTO.class);

        // Convert and set ArtistDTO
        songDTO.setArtist(modelMapper.map(song.getArtist(), ArtistDTO.class));

        // Convert and set AlbumDTO
        songDTO.setAlbum(modelMapper.map(song.getAlbum(), AlbumDTO.class));

        return songDTO;
    }

}
