package by.rucvald.homework_5.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${kafka.topic}")
    public void listenNotificationAndSendMessage(@Header(KafkaHeaders.RECEIVED_KEY) String key, String email) {
        try {
            if ("create".equals(key)) {
                emailService.sendEmail(email, "Account", "Account created");
            }
            if ("delete".equals(key)) {
                emailService.sendEmail(email, "Account", "Account deleted");
            }
            else {
                logger.info("Unknown key: {}", key);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred when trying to send email: " + e.getMessage());
        }
    }
}
