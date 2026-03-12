package com.techdeals.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.net.URI;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Override
    public ClientConfiguration clientConfiguration() {
        try {
            URI uri = new URI(elasticsearchUri);
            String hostAndPort = uri.getHost() + ":" + uri.getPort();
            return ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .build();
        } catch (Exception e) {
            // Fallback to string manipulation if URI parsing fails
            String hostAndPort = elasticsearchUri
                .replaceFirst("https?://", "");
            return ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .build();
        }
    }
}
