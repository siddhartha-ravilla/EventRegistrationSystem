package com.eventregistration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class AwsConfig {
    
    @Value("${aws.region:us-east-1}")
    private String region;
    
    @Value("${aws.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;
    
    @Bean
    public DynamoDbClient dynamoDbClient() {
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            return DynamoDbClient.builder()
                    .region(Region.of(region))
                    .build();
        } else {
            return DynamoDbClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .build();
        }
    }
    
    @Bean
    public SqsClient sqsClient() {
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            return SqsClient.builder()
                    .region(Region.of(region))
                    .build();
        } else {
            return SqsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .build();
        }
    }
    
    @Bean
    public SnsClient snsClient() {
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            return SnsClient.builder()
                    .region(Region.of(region))
                    .build();
        } else {
            return SnsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .build();
        }
    }
    
    @Bean
    public SesClient sesClient() {
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            return SesClient.builder()
                    .region(Region.of(region))
                    .build();
        } else {
            return SesClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .build();
        }
    }
    
    @Bean
    public LambdaClient lambdaClient() {
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            return LambdaClient.builder()
                    .region(Region.of(region))
                    .build();
        } else {
            return LambdaClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                    .build();
        }
    }
} 