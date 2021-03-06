package org.rublin;

import lombok.extern.slf4j.Slf4j;
import org.rublin.service.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledTasks {

    @Autowired
    private TelegramBotService botService;

    @Scheduled(cron = "0 1 12 6 * *")
//    @Scheduled(fixedRate = 60000)
    public void askForDonation() {
        String message = "Do you like to use this bot? I hope so...\n" +
                "Address (KRB) for donate\n\n" +
                "*KaAxHCPtJaFGDq4xLn3fASf3zVrAmqyE4359zn3r3deVjCeM3CYq7K4Y1pkfZkjfRd1W2VPXVZdA5RBdpc4Vzamo1H4F5qZ*";
        log.info("Ask for donation: \n {}", message);
        botService.sendMessage(message);
    }
}
