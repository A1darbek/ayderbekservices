package com.ayderbek.common;


import lombok.Data;


@Data
public class SongDTO {
    private Long id;
    private String title;
    private ArtistDTO artist;
    private AlbumDTO album;
    private String length;
    private String genre;
    private String signedUrl;
    private String thumbnail;
}
