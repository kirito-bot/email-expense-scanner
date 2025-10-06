package com.example.email_expense_scanner;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class GmailReaderService {

    private static final Logger logger = Logger.getLogger(GmailReaderService.class.getName());
    private final Gmail gmail;

    public GmailReaderService(Gmail gmail) {
        this.gmail = gmail;
    }

    public void listUnreadMessages() throws Exception {
        ListMessagesResponse response = gmail.users().messages().list("me")
                .setQ("label:strava")
                .setMaxResults(1L)
                .execute();

        List<Message> messages = response.getMessages();

        if (messages == null || messages.isEmpty()) {
            logger.info("No unread emails found.");
            return;
        }

        for (Message m : messages) {
            Message fullMsg = gmail.users().messages().get("me", m.getId())
                    .setFormat("metadata")
                    .setMetadataHeaders(Collections.singletonList("Subject"))
                    .execute();

            logger.info("ID: " + m.getId() +
                    " | Subject: " + fullMsg.getPayload().getHeaders().getFirst().getValue());
        }
    }
}
