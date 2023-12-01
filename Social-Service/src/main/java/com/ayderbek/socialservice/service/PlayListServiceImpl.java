package com.ayderbek.socialservice.service;

import com.ayderbek.common.PlaylistDTO;
import com.ayderbek.socialservice.Request.PlaylistRequest;
import com.ayderbek.socialservice.S3.SocialS3Config;
import com.ayderbek.socialservice.S3.SocialS3Service;
import com.ayderbek.socialservice.domain.Playlist;
import com.ayderbek.socialservice.repository.PlayListRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayListServiceImpl implements PlayListService{
    private final PlayListRepository playlistRepository;
    private final SocialS3Service socialS3Service;
    private final SocialS3Config s3Buckets;
    private final ModelMapper modelMapper;


    @Override
    public PlaylistDTO createPlaylist(PlaylistRequest playlistRequest, MultipartFile coverArtImage) {
        Playlist playlist = convertToPlayList(playlistRequest);
        if (coverArtImage != null) {
            String thumbnailKey = uploadSongCoverArtImageToS3(playlist.getId(), coverArtImage);
            playlist.setCoverArt(thumbnailKey);
        }
        Playlist savedPlayList = playlistRepository.save(playlist);

        return convertToPlaylistDTO(savedPlayList);
    }

    @Override
    public PlaylistDTO getPlaylistById(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        return modelMapper.map(playlist, PlaylistDTO.class);
    }

    @Override
    public List<PlaylistDTO> getAllPlaylists() {
        List<Playlist> playlists = playlistRepository.findAll();

        // Use Java streams to map the list of Playlist entities to a list of PlaylistDTOs
        return playlists.stream()
                .map(playlist -> modelMapper.map(playlist, PlaylistDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public String uploadSongCoverArtImageToS3(Long playListId, MultipartFile file) {
        String coverArtId = UUID.randomUUID().toString();
        try {
            byte[] coverArtBytes = file.getBytes();
            String s3Key = "PlaysListCoverArt-images/%s/%s".formatted(playListId, coverArtId);
            socialS3Service.putObject(
                    s3Buckets.getSpotify(),
                    s3Key,
                    file.getInputStream(), coverArtBytes
            );
            return s3Key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload CoverArt image", e);
        }
    }

    @Override
    public PlaylistDTO convertToPlaylistDTO(Playlist playlist) {
        return modelMapper.map(playlist, PlaylistDTO.class);
    }

    @Override
    public PlaylistDTO getPlaylistWithDetails(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Song with ID [%s] not found".formatted(playlistId)));

        String coverArtBytes = getPlaylistCovertArtUrl(playlistId);

        PlaylistDTO playlistDTO = convertToPlaylistDTO(playlist);

        playlistDTO.setCoverArt(coverArtBytes);

        return playlistDTO;
    }

    @Override
    public String getPlaylistCovertArtUrl(Long playlistId) {
        PlaylistDTO playlist = getPlaylistById(playlistId);
        return playlist.getCoverArt();
    }

    @Override
    public Playlist convertToPlayList(PlaylistRequest playlistRequest) {
        Playlist playlist = new Playlist();
        playlist.setTitle(playlistRequest.getTitle());
        playlist.setSongIds(playlistRequest.getSongIds());
        playlist.setCreatorId(playlistRequest.getCreatorId());
        return playlist;
    }
}
