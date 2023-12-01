package com.ayderbek.socialservice.Request;

import lombok.Data;

import java.util.List;

@Data
public class PlaylistRequest {
    private String title;
    private Long creatorId;
    private String coverArt;
    private List<Long> songIds;
}
