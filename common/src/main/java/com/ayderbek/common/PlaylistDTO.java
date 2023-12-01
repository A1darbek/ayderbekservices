package com.ayderbek.common;

import lombok.Data;

import java.util.List;

@Data
public class PlaylistDTO {
    private Long id;
    private String title;
    private Long creatorId;
    private String coverArt;
    private List<Long> songIds;
}
