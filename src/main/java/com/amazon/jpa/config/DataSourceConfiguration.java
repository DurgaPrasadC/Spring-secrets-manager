package com.amazon.jpa.config;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DataSourceConfiguration {

    @Resource
    private Environment env;
    
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfiguration.class);	

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties appDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource appDataSource() {

		String secretName = env.getProperty("spring.aws.secretsmanager.secretName");
		String endpoint = env.getProperty("spring.aws.secretsmanager.endpoint");
		String region = env.getProperty("spring.aws.secretsmanager.region");

		AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
		AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
		clientBuilder.setEndpointConfiguration(config);
		AWSSecretsManager client = clientBuilder.build();


		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode secretsJson = null;

		ByteBuffer binarySecretData;
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
				.withSecretId(secretName);
		GetSecretValueResult getSecretValueResponse = null;
		try {
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

		} catch(ResourceNotFoundException e) {
			log.error("The requested secret " + secretName + " was not found");
		} catch (InvalidRequestException e) {
			log.error("The request was invalid due to: " + e.getMessage());
		} catch (InvalidParameterException e) {
			log.error("The request had invalid params: " + e.getMessage());
		}

		if(getSecretValueResponse == null) {
			return null;
		}

		// Decrypted secret using the associated KMS CMK
		// Depending on whether the secret was a string or binary, one of these fields will be populated
		String secret = getSecretValueResponse.getSecretString();
//		if(secret == null) {
//			log.error("The Secret String returned is null");
//			return null;
//		}
//			try {
//				secretsJson = objectMapper.readTree(secret);
//			} catch (IOException e) {
//				log.error("Exception while retreiving secret values: " + e.getMessage());
//			}
//
//
//		System.out.println("Secrets json - "+secretsJson);
//		String host = secretsJson.get("host").textValue();
//		String port = secretsJson.get("port").textValue();
//		String dbname = secretsJson.get("dbname").textValue();
//		String username = secretsJson.get("username").textValue();
//		String password = secretsJson.get("password").textValue();
		appDataSourceProperties().setUrl("jdbc:postgresql://localhost:5432/");
		appDataSourceProperties().setUsername("postgres");
		appDataSourceProperties().setPassword(secret);

        return appDataSourceProperties().initializeDataSourceBuilder().build();
    }
}
