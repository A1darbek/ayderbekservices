package com.queryservice.client;

import com.ayderbek.common.PlaylistDTO;
import com.queryservice.security.OAuthFeignConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "capital-service",url = "http://localhost:8083",configuration = OAuthFeignConfig.class)
public interface SocialServiceFeignClient {


    @RequestMapping(method = RequestMethod.GET,value = "/playlists/{playlistId}")
    PlaylistDTO getPlaylistDetails(@PathVariable("playlistId") Long playlistId);
}
