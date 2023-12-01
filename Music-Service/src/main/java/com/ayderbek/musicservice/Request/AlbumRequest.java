package com.ayderbek.musicservice.Request;

import lombok.Data;

@Data
public class AlbumRequest {
    private String title;
    private Long artistId;
    private Integer year;
    private String genre;
    private String coverArt;
}
