package com.ayderbek.socialservice.S3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws.s3.buckets")
@Getter
@Setter
public class SocialS3Config {
    private String spotify;
}
