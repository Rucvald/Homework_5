package by.rucvald.homework_5;

import by.rucvald.homework_5.service.EmailService;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "test-topic", brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092", "port=9092"
})
class NotificationServiceIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps("localhost:9092");
        ProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProps, new StringSerializer(), new StringSerializer());
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @AfterEach
    void tearDown() {
        // Очистка моков
        reset(emailService);
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("kafka.topic", () -> "test-topic");
    }

    @Test
    void testCreateMessageTriggersEmail() throws Exception {
        String email = "test@example.com";
        kafkaTemplate.send("test-topic", "create", email);

        verify(emailService, times(1)).sendEmail(email, "Account", "Account created");
    }

    @Test
    void testDeleteMessageTriggersEmail() throws Exception {
        String email = "delete@example.com";
        kafkaTemplate.send("test-topic", "delete", email);
        Thread.sleep(1000);

        verify(emailService, times(1)).sendEmail(email, "Account", "Account deleted");
    }

    @Test
    void testUnknownKeyDoesNotTriggerEmail() throws Exception {
        String email = "unknown@example.com";
        kafkaTemplate.send("test-topic", "update", email);
        Thread.sleep(1000);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
