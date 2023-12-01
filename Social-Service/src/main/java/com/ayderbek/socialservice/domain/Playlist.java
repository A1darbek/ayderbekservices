package com.ayderbek.socialservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "creator_id")
    private Long creatorId;

    private String coverArt;

    @ElementCollection
    @CollectionTable(name = "playlist_song_ids", joinColumns = @JoinColumn(name = "playlist_id"))
    @Column(name = "song_id")
    private List<Long> songIds;
}
