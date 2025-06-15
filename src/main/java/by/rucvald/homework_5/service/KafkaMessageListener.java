package by.rucvald.homework_5.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageListener implements CommandLineRunner {

    private final NotificationService notificationService;

    public KafkaMessageListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void run(String... args) throws Exception {
        notificationService.send();
    }
}
