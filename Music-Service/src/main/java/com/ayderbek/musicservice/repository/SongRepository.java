package com.ayderbek.musicservice.repository;

import com.ayderbek.musicservice.domain.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SongRepository extends JpaRepository<Song,Long> {

    @Query(value = "SELECT count(id) FROM song WHERE id = ?", nativeQuery = true)
    Boolean existsSongById(Long id);
}
