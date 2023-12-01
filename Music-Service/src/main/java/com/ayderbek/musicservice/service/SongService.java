package com.ayderbek.musicservice.service;

import com.amazonaws.services.alexaforbusiness.model.NotFoundException;
//import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import com.ayderbek.common.AlbumDTO;
import com.ayderbek.common.ArtistDTO;
import com.ayderbek.common.SongDTO;
import com.ayderbek.musicservice.Request.SongRequest;
import com.ayderbek.musicservice.S3.S3Config;
import com.ayderbek.musicservice.S3.S3Service;
import com.ayderbek.musicservice.domain.Album;
import com.ayderbek.musicservice.domain.Artist;
import com.ayderbek.musicservice.domain.Song;
import com.ayderbek.musicservice.exception.ResourceNotFoundException;
import com.ayderbek.musicservice.repository.AlbumRepository;
import com.ayderbek.musicservice.repository.ArtistRepository;
import com.ayderbek.musicservice.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.amazonaws.services.cloudfront.CloudFrontUrlSigner.getSignedURLWithCustomPolicy;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongService {
    private final SongRepository songRepository;
    private final S3Service s3Service;
    private final S3Config s3Buckets;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final ModelMapper modelMapper;




    public Song getById(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() -> new NotFoundException("Song not found with id " + songId));
    }

    private void checkIfSongExistsOrThrow(Long songId) {
        Boolean exists = songRepository.existsSongById(songId);
        if (exists == null || !exists) {
            throw new ResourceNotFoundException(
                    "Song with id [%s] not found".formatted(songId)
            );
        }
    }

    public byte[] getSongThumbnailImage(Long songId) {
        Song song = getById(songId);
        String cloudFrontUrl = song.getThumbnail();

        try {
            // Retrieve the image directly from CloudFront
            URL url = new URL(cloudFrontUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            byte[] thumbnailBytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            return thumbnailBytes;
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve thumbnail image from CloudFront", e);
        }
    }


    public SongDTO createSong(
            SongRequest songRequest,
            MultipartFile imageFile,
            MultipartFile songFile
    ) {
        // Convert the SongRequest to a Song entity
        Song song = convertToSong(songRequest);

        // Validate and set the artist and album for the song
        validateAndSetArtist(song, songRequest.getArtistId());
        validateAndSetAlbum(song, songRequest.getAlbumId());

        // Upload the song file to S3 with CloudFront
        String songFileKey = uploadSongFileToS3(song.getId(), songFile);
        song.setKey(songFileKey);

        // Upload the song thumbnail image to S3 with CloudFront
        if (imageFile != null) {
            String thumbnailKey = uploadSongThumbnailImageToS3(song.getId(), imageFile);
            song.setThumbnail(thumbnailKey);
        }

        // Save the song entity in the database
        Song savedSong = songRepository.save(song);

        // Convert the saved song to a SongDTO and return
        return convertToSongDTO(savedSong);
    }
    // ------------------------------------------------------------------------- NEW

    public String uploadSongFileToS3(Long songId, MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            String s3Key = "song-files/%s/%s".formatted(songId, originalFileName);
            s3Service.putObject(
                    s3Buckets.getSpotify(),
                    s3Key,
                    file.getInputStream()
            );
            return s3Key;
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to upload song file", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String uploadSongThumbnailImageToS3(Long songId, MultipartFile file) {
        String thumbnailId = UUID.randomUUID().toString();
        try {
            byte[] thumbnailBytes = file.getBytes();
            String s3Key = "thumbnail-images/%s/%s".formatted(songId, thumbnailId);
            s3Service.putObject(
                    s3Buckets.getSpotify(),
                    s3Key,
                    file.getInputStream(), thumbnailBytes
            );
            return s3Key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload thumbnail image", e);
        }
    }


    public SongDTO updateSong(Long id, SongRequest songRequest) {
        // Retrieve the existing song entity by ID from the database
        Song existingSong = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song with ID [%s] not found".formatted(id)));

        // Update the existing song entity with the data from the SongRequest
        updateSongFromRequest(existingSong, songRequest);

        // Validate and set the artist and album for the song
        validateAndSetArtist(existingSong, songRequest.getArtistId());
        validateAndSetAlbum(existingSong, songRequest.getAlbumId());

        // Save the updated song entity in the database
        Song updatedSong = songRepository.save(existingSong);

        // Convert the updated song to a SongDTO and return
        return convertToSongDTO(updatedSong);
    }

    public void deleteSong(Long id) {
        // Check if the song exists
        if (!songRepository.existsById(id)) {
            throw new ResourceNotFoundException("Song with ID [%s] not found".formatted(id));
        }

        // Delete the song entity from the database
        songRepository.deleteById(id);
    }

    private Song convertToSong(SongRequest songRequest) {
        Song song = new Song();
        song.setTitle(songRequest.getTitle());
        song.setLength(songRequest.getLength());
        song.setGenre(songRequest.getGenre());
        return song;
    }

    private void updateSongFromRequest(Song song, SongRequest songRequest) {
        if (songRequest.getTitle() != null) {
            song.setTitle(songRequest.getTitle());
        }
        if (songRequest.getLength() != null) {
            song.setLength(songRequest.getLength());
        }
        if (songRequest.getGenre() != null) {
            song.setGenre(songRequest.getGenre());
        }
    }

    private void validateAndSetArtist(Song song, Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist with ID [%s] not found".formatted(artistId)));
        song.setArtist(artist);
    }

    // Helper method to validate and set the album for the song
    private void validateAndSetAlbum(Song song, Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album with ID [%s] not found".formatted(albumId)));
        song.setAlbum(album);
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
