package com.ayderbek.musicservice.service;

import com.ayderbek.musicservice.config.QueryType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {
    private final RestTemplate restTemplate;


    public String search(String query, QueryType queryType) {
        try {
            String elasticsearchUrl = "http://localhost:9200/newwavesongs/_search";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            String requestBody = switch (queryType) {
                case WILDCARD -> buildWildcardQueryRequestBody(query);
                case MULTI_MATCH -> buildMultiMatchQueryRequestBody(query);
                case FUZZY -> buildFuzzyQueryRequestBody(query);
                // Add more cases for other query types
            };

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = getResponseEntity(elasticsearchUrl, entity);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("Elasticsearch request failed.");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Elasticsearch request failed.");
        }
    }

    private ResponseEntity<String> getResponseEntity(String elasticsearchUrl, HttpEntity<String> entity) {
        return restTemplate.exchange(
                elasticsearchUrl,
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    private String buildWildcardQueryRequestBody(String query) {
        return "{\"query\": {\"wildcard\": {\"title\": \"*" + query + "*\"}}}";
    }

    private String buildMultiMatchQueryRequestBody(String query) {
        return "{\"query\": {\"multi_match\": {\"query\": \"" + query + "\", \"fields\": [\"title\", \"artist_name\", \"album_name\"]}}, \"size\": 10}";
    }

    private String buildFuzzyQueryRequestBody(String query) {
        return "{\"query\": {\"fuzzy\": {\"title\": \"" + query + "\"}}}";
    }
}
