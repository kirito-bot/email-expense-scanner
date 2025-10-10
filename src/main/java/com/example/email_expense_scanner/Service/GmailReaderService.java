package com.example.email_expense_scanner.Service;

import com.example.email_expense_scanner.Modal.ExpenseModal;
import com.example.email_expense_scanner.Utils.GmailUtilsService;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;


@Service
public class GmailReaderService {

    private final GmailUtilsService gmailUtilsService;
    private final NlpExpenseService nlpExpenseService;
    private static final Logger logger = Logger.getLogger(GmailReaderService.class.getName());
    private final Gmail gmail;

    @Autowired
    private ThreadPoolTaskExecutor asyncExecutor;

    public GmailReaderService(@Autowired GmailUtilsService gmailUtilsService, @Autowired NlpExpenseService nlpExpenseService, Gmail gmail) {
        this.gmailUtilsService = gmailUtilsService;
        this.nlpExpenseService = nlpExpenseService;
       // this.asyncConfig = asyncConfig;
        this.gmail = gmail;
    }

    public void listUnreadMessages() throws Exception {
        ListMessagesResponse response = gmail.users().messages()
                .list("me").setQ("label:chase").setMaxResults(1L).execute();

        List<Message> messages = response.getMessages();

        if (messages == null || messages.isEmpty()) {
            logger.info("No unread emails found.");
            return;
        }
        List<CompletableFuture<ExpenseModal.ExpenseResult>> future = new ArrayList<>();
        for (Message m : messages) {
            Message fullMsg = gmail.users().messages().get("me", m.getId()).setFormat("full").execute();

            String emailBody = gmailUtilsService.getEmailBody(fullMsg);
            future.add(nlpExpenseService.analyzeEmailAsync(emailBody));
        }
        CompletableFuture.allOf(future.toArray(new CompletableFuture[0])).join();

        // Gather results
        future.stream()
                .map(CompletableFuture::join)
                .forEach(result -> logger.info("\n"+result.toString() +"\n"));
        asyncExecutor.shutdown();

 //           Document document = Jsoup.parse(gmailUtilsService.getEmailBody(fullMsg));
//            Element table = document.selectFirst("table");
//
//            for(Element row : table.select("tr")) {
//                Elements cols = row.select("td");
//                for (Element col : cols) {
//
//                }
//            }
//            logger.info("ID: " + m.getId() +
//                    " | Subject: " + gmailUtilsService.getHeader(fullMsg, "SUBJECT") +
//                    " | Body: " + gmailUtilsService.parseExpenseTable(gmailUtilsService.getEmailBody(fullMsg)));
            // " | Clean Body: " + cleanBody);

    }
}
