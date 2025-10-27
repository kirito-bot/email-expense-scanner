package com.example.EmailExpenseScanner.Service;

import com.example.EmailExpenseScanner.Modal.ExpenseModal;
import com.example.EmailExpenseScanner.Utils.CSVWriterService;
import com.example.EmailExpenseScanner.Utils.GmailUtilsService;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class ProcessEmailService {

    private static final Logger logger = Logger.getLogger(ProcessEmailService.class.getName());
    private final GmailUtilsService gmailUtilsService;
    private final FindExpenseService findExpenseService;
    private final CSVWriterService csvWriterService;
    private final Gmail gmail;

    volatile int messagesProcessed = 1;

    public ProcessEmailService(@Autowired GmailUtilsService gmailUtilsService, @Autowired FindExpenseService findExpenseService, @Autowired CSVWriterService csvWriterService, @Autowired Gmail gmail) {
        this.gmailUtilsService = gmailUtilsService;
        this.findExpenseService = findExpenseService;
        this.csvWriterService = csvWriterService;
        this.gmail = gmail;
    }

    public void processEmail(List<Message> messages, int batch) {
        logger.info("Found " + messages.size() + "  emails to process.");
        List<CompletableFuture<ExpenseModal.ExpenseEntry>> future = new ArrayList<>();

        for (Message m : messages) {
            synchronized (this) {
                logger.info("Processing message No : " + messagesProcessed++);
            }
            Message fullMsg;
            try {
                fullMsg = gmail.users().messages().get("me", m.getId()).setFormat("full").execute();
            } catch (IOException e) {
                logger.severe("Failed to fetch full message for ID: " + m.getId() + " Error: " + e);
                continue;
            }

            String emailBody = gmailUtilsService.getEmailBody(fullMsg);
            logger.info("Email Body: ------------------------------------->" + emailBody);
            future.add(findExpenseService.analyzeEmailAsync(emailBody, m.getId()));
        }
        CompletableFuture.allOf(future.toArray(new CompletableFuture[0])).join();

        // Gather results
        List<ExpenseModal.ExpenseEntry> results = future.stream()
                .map(CompletableFuture::join).toList();
        logger.info("Total results: " + results.size());
        for (Object result : results) {
            logger.info("TEST \n" + result.toString() + "\n");
        }
        // POST PROCESSING

        csvWriterService.writeToCSV(results, batch);
    }
}
