package com.ayderbek.musicservice.repository;

import com.ayderbek.musicservice.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album,Long> {
}
