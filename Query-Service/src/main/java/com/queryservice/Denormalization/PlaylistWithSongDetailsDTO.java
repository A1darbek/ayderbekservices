package com.queryservice.Denormalization;

import com.ayderbek.common.SongDTO;
import lombok.Data;

import java.util.List;


@Data
public class PlaylistWithSongDetailsDTO {
    private Long id;
    private String title;
    private Long creatorId;
    private String coverArt;
    private List<SongDTO> songs;
}

