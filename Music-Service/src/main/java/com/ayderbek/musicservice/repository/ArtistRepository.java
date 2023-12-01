package com.ayderbek.musicservice.repository;

import com.ayderbek.musicservice.domain.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistRepository extends JpaRepository<Artist,Long> {
}
