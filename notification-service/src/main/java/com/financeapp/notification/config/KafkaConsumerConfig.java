package com.financeapp.notification.config;

import com.financeapp.notification.event.PayrollProcessedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for notification-service.
 *
 * <h2>Key Choices</h2>
 * <ul>
 *   <li><strong>AckMode.MANUAL</strong> — offset only committed after all notification
 *       actions succeed. Prevents event loss if the service crashes mid-processing.</li>
 *   <li><strong>concurrency = 3</strong> — one thread per topic partition.
 *       Allows parallel processing of events from different payroll periods.</li>
 *   <li><strong>DefaultErrorHandler with FixedBackOff</strong> — retries up to 3 times
 *       with 1 second delay before sending to a Dead Letter Topic (if configured).</li>
 *   <li><strong>TRUSTED_PACKAGES</strong> — restricts Jackson type resolution to our
 *       own packages for security.</li>
 * </ul>
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, PayrollProcessedEvent> consumerFactory() {
        JsonDeserializer<PayrollProcessedEvent> deserializer =
                new JsonDeserializer<>(PayrollProcessedEvent.class);
        deserializer.addTrustedPackages("com.financeapp.*");
        // Don't require the type header — producer sends spring.json.add.type.headers=false
        deserializer.setUseTypeHeaders(false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Manual offset commit — we control exactly when the offset advances
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // Read from the beginning on first start (no prior committed offset)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Max 10 records per poll to keep processing latency predictable
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PayrollProcessedEvent>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, PayrollProcessedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Manual offset acknowledgement — listener calls ack.acknowledge() explicitly
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // 3 concurrent consumer threads — one per partition
        factory.setConcurrency(3);

        // Retry up to 3 times with 1 second fixed backoff before giving up
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(new FixedBackOff(1_000L, 3L))
        );

        return factory;
    }
}
