package com.ayderbek.common;

import lombok.Data;

@Data
public class AlbumDTO {
    private Long id;
    private String title;
    private Long artistId;
    private Integer year;
    private String genre;
    private String coverArt;
}
