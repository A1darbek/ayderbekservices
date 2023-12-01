package com.ayderbek.socialservice.repository;

import com.ayderbek.socialservice.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayListRepository extends JpaRepository<Playlist,Long> {
}
