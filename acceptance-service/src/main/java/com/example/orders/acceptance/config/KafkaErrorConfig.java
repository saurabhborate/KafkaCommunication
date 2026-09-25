package com.example.orders.acceptance.config;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.kafka.core.KafkaTemplate; import org.springframework.kafka.listener.CommonErrorHandler; import org.springframework.kafka.listener.DeadLetterPublishingRecoverer; import org.springframework.kafka.listener.DefaultErrorHandler; import org.springframework.util.backoff.FixedBackOff;
@Configuration public class KafkaErrorConfig {
 @Bean CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String,String> template){
  DeadLetterPublishingRecoverer recoverer=new DeadLetterPublishingRecoverer(template,(record,ex)->new org.apache.kafka.common.TopicPartition(record.topic()+".dlq",record.partition()));
  return new DefaultErrorHandler(recoverer,new FixedBackOff(1000L,2L));
 }
}
