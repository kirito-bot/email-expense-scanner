package com.example.email_expense_scanner;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;


@Service
public class GmailReaderService {

    private final GmailUtilsService gmailUtilsService;
    private static final Logger logger = Logger.getLogger(GmailReaderService.class.getName());
    private final Gmail gmail;

    public GmailReaderService(@Autowired GmailUtilsService gmailUtilsService, Gmail gmail) {
        this.gmailUtilsService = gmailUtilsService;
        this.gmail = gmail;
    }

    public void listUnreadMessages() throws Exception {
        ListMessagesResponse response = gmail.users().messages().list("me").setQ("label:chase").setMaxResults(1L).execute();

        List<Message> messages = response.getMessages();

        if (messages == null || messages.isEmpty()) {
            logger.info("No unread emails found.");
            return;
        }

        for (Message m : messages) {
            Message fullMsg = gmail.users().messages().get("me", m.getId()).setFormat("full").execute();
            gmailUtilsService.parseExpenseTable(gmailUtilsService.getEmailBody(fullMsg));
            // String cleanBody = Jsoup.parse(gmailUtilsService.getEmailBody(fullMsg)).text();
//
//            logger.info("ID: " + m.getId() +
//                    " | Subject: " + gmailUtilsService.getHeader(fullMsg, "SUBJECT") +
//                    " | Body: " + gmailUtilsService.parseExpenseTable(gmailUtilsService.getEmailBody(fullMsg)));
            // " | Clean Body: " + cleanBody);
        }
    }
}
