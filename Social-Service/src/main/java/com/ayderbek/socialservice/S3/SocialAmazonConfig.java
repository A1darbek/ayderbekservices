package com.ayderbek.socialservice.S3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class SocialAmazonConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    public S3Client socials3Client() {
        S3Client client = S3Client.builder()
                .region(Region.of(awsRegion))
                .build();
        return client;
    }
}
