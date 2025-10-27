package com.example.e_learning_system.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

    @Value("${aws.access-key-id}")
    private String accessKey;

    @Value("${aws.secret-access-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public StaticCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
    }

    @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public S3Client s3Client(StaticCredentialsProvider credentialsProvider, Region region) {
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(StaticCredentialsProvider credentialsProvider, Region region) {
        return S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }
}
