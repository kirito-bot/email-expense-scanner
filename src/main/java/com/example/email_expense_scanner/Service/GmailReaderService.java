package com.example.email_expense_scanner.Service;

import com.example.email_expense_scanner.Modal.ExpenseModal;
import com.example.email_expense_scanner.Utils.GmailUtilsService;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
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
    private final FindExpenseService findExpenseService;
    private static final Logger logger = Logger.getLogger(GmailReaderService.class.getName());
    private final Gmail gmail;
    private final ThreadPoolTaskExecutor asyncExecutor;

    public GmailReaderService(@Autowired GmailUtilsService gmailUtilsService, @Autowired FindExpenseService findExpenseService, @Autowired ThreadPoolTaskExecutor asyncExecutor, Gmail gmail) {
        this.gmailUtilsService = gmailUtilsService;
        this.findExpenseService = findExpenseService;
        this.asyncExecutor = asyncExecutor;
        this.gmail = gmail;
    }

    volatile int messagesProcessed = 1;
    public void listUnreadMessages() throws Exception {

        ListMessagesResponse response = gmail.users().messages()
                .list("me").setQ("label:chase").setMaxResults(1L)
                .execute();

        List<Message> messages = response.getMessages();
        logger.info("Found " + (messages == null ? 0 : messages.size()) + "  emails to process.");
        if (messages == null || messages.isEmpty()) {
            logger.info("No unread emails found.");
            return;
        }
        List<CompletableFuture<ExpenseModal.ExpenseResult>> future = new ArrayList<>();

        for (Message m : messages) {
            synchronized (this) {
                logger.info("Processing message No : " + messagesProcessed++);
            }
            Message fullMsg = gmail.users().messages().get("me", m.getId()).setFormat("full").execute();

            String emailBody = gmailUtilsService.getEmailBody(fullMsg);
            logger.info("Email Body: ------------------------------------->" + emailBody);
            future.add(findExpenseService.analyzeEmailAsync(emailBody));
        }
        CompletableFuture.allOf(future.toArray(new CompletableFuture[0])).join();

        // Gather results
        List results = future.stream()
                .map(CompletableFuture::join).toList();
        logger.info("Total results: " + results.size());
        for (Object result : results) {
            logger.info("TEST TEST \n" + result.toString() + "\n");
        }

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
