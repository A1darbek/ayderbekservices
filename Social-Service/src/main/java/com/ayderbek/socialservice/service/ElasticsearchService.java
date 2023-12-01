package com.ayderbek.socialservice.service;

import com.ayderbek.socialservice.config.QueryType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

public interface ElasticsearchService {
    String search(String query, QueryType queryType);

    ResponseEntity<String> getResponseEntity(String elasticsearchUrl, HttpEntity<String> entity);

    String buildWildcardQueryRequestBody(String query);

    String buildFuzzyQueryRequestBody(String query);
}
