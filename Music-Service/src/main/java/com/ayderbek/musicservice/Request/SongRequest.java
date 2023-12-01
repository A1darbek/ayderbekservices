package com.ayderbek.musicservice.Request;

import lombok.Data;

@Data
public class SongRequest {
    private String title;
    private Long artistId;
    private Long albumId;
    private String length;
    private String genre;
}
