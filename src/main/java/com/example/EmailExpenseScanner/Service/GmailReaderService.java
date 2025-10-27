package com.example.EmailExpenseScanner.Service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;


@Service
public class GmailReaderService {

    private static final Logger logger = Logger.getLogger(GmailReaderService.class.getName());
    private final Gmail gmail;
    private final ProcessEmailService processEmailService;
    private final ThreadPoolTaskExecutor asyncExecutor;
    private int batch = 1;


    public GmailReaderService(Gmail gmail, @Autowired ProcessEmailService processEmailService, @Autowired ThreadPoolTaskExecutor asyncExecutor) {
        this.gmail = gmail;
        this.processEmailService = processEmailService;
        this.asyncExecutor = asyncExecutor;
    }

    public void listUnreadMessages() throws Exception {
        String pageToken = null;

        do {
            ListMessagesResponse response = gmail.users().messages()
                    .list("me").setQ("label:chase").setMaxResults(1L).setPageToken(pageToken)
                    .execute();
            if (response.getMessages() != null && !response.getMessages().isEmpty()) {
                logger.info("Processing batch number: " + batch + " with " + response.getMessages().size() + " messages.");
                processEmailService.processEmail(response.getMessages(), batch);
                batch++;
            } else {
                logger.info("No messages found in this batch.");
            }
            pageToken = response.getNextPageToken();
            if (batch > 3) break; // Limit to 3 batches for testing
        } while (pageToken != null);

        asyncExecutor.shutdown();
    }
}
