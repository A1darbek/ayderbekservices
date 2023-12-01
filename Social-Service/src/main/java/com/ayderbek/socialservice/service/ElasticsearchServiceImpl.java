package com.ayderbek.socialservice.service;

import com.ayderbek.socialservice.config.QueryType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ElasticsearchServiceImpl implements ElasticsearchService {
    private final RestTemplate restTemplate;


    @Override
    public String search(String query, QueryType queryType) {
        try {
            String elasticsearchUrl = "http://localhost:9200/newwavesongs/_search";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            String requestBody = switch (queryType) {
                case WILDCARD -> buildWildcardQueryRequestBody(query);
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

    @Override
    public ResponseEntity<String> getResponseEntity(String elasticsearchUrl, HttpEntity<String> entity) {
        return restTemplate.exchange(
                elasticsearchUrl,
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    @Override
    public String buildWildcardQueryRequestBody(String query) {
        return "{\"query\": {\"wildcard\": {\"title.keyword\": \"*" + query + "*\"}}}";
    }

    @Override
    public String buildFuzzyQueryRequestBody(String query) {
        return "{\"query\": {\"fuzzy\": {\"title.keyword\": \"" + query + "\"}}}";
    }
}
