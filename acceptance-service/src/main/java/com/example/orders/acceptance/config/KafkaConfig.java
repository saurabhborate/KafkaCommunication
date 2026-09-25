package com.example.orders.acceptance.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean NewTopic acceptedTopic() { return TopicBuilder.name("orders.accepted").partitions(3).replicas(1).build(); }
    @Bean NewTopic processedTopic() { return TopicBuilder.name("orders.processed").partitions(3).replicas(1).build(); }
    @Bean NewTopic acceptedDlq() { return TopicBuilder.name("orders.accepted.dlq").partitions(3).replicas(1).build(); }
    @Bean NewTopic processedDlq() { return TopicBuilder.name("orders.processed.dlq").partitions(3).replicas(1).build(); }
}
