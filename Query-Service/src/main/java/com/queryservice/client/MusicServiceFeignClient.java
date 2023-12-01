package com.queryservice.client;

import com.ayderbek.common.SongDTO;
import com.queryservice.security.OAuthFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "music-service",url = "http://localhost:8083",configuration = OAuthFeignConfig.class)
public interface MusicServiceFeignClient {
    @GetMapping("/songs/{songId}")
    ResponseEntity<SongDTO> getSongDetails(@PathVariable("songId") Long songId);
}
