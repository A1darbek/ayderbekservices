package com.ayderbek.socialservice.service;

import com.ayderbek.common.PlaylistDTO;
import com.ayderbek.socialservice.Request.PlaylistRequest;
import com.ayderbek.socialservice.domain.Playlist;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlayListService {
    PlaylistDTO createPlaylist(PlaylistRequest playlistRequest, MultipartFile coverArtImage);

    PlaylistDTO getPlaylistById(Long playlistId);

    List<PlaylistDTO> getAllPlaylists();

    Playlist convertToPlayList(PlaylistRequest playlistRequest);

    String uploadSongCoverArtImageToS3(Long songId, MultipartFile file);

    PlaylistDTO convertToPlaylistDTO(Playlist playlist);

    PlaylistDTO getPlaylistWithDetails(Long playlistId);

    String getPlaylistCovertArtUrl(Long playlistId);
}
