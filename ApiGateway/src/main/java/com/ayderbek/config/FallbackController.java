package com.ayderbek.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Log4j2
public class FallbackController {


    @GetMapping("/fallback/song/stream")
    public ResponseEntity<String> getFallbackSongStream() {
        // Check if fallback response is available in the cache


        // Create a JSON object with the fallback response
        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("error", true);
        fallbackResponse.put("message", "Unable to get the signed URL for songId " );
        fallbackResponse.put("cachedData", "Cached Data for songId " );

        try {
            // Convert the JSON object to a String
            ObjectMapper objectMapper = new ObjectMapper();
            String responseBody = objectMapper.writeValueAsString(fallbackResponse);
            ResponseEntity<String> fallbackEntity = ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(responseBody);

            // Cache the fallback response

            return fallbackEntity;
        } catch (JsonProcessingException e) {
            log.error("Error converting fallback response to JSON", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to get the signed URL for songId ");
        }
    }


}

