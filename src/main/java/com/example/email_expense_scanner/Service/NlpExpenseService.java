package com.example.email_expense_scanner.Service;

import com.example.email_expense_scanner.Modal.ExpenseModal;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NlpExpenseService {
    private static final Logger logger = Logger.getLogger(NlpExpenseService.class.getName());
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\$\\d+(\\.\\d{1,2})?");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{2,4})");

    @Async
    public CompletableFuture<ExpenseModal.ExpenseResult> analyzeEmailAsync(String htmlBody) {
        try {
            Document doc = Jsoup.parse(htmlBody);
            List<ExpenseModal.ExpenseEntry> entries = new ArrayList<>();

            for (Element table : doc.select("table")) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    Elements cols = row.select("td");
                    if (cols.isEmpty()) continue;

                    String text = cols.text();

                    String merchant = detectMerchant(text);
                    String amount = detectAmount(text);
                    String date = detectDate(text);

                    if (merchant != null) {
                        logger.info("Detected - Merchant: " + merchant);
                    }
                    if (amount != null) {
                        logger.info("Amount: " + amount);
                    }
                    if (date != null) {
                        logger.info("Date: " + date);
                    }

                }
            }

//            for(ExpenseModal.ExpenseEntry entry : entries) {
//
//            }

            return CompletableFuture.completedFuture(new ExpenseModal.ExpenseResult(entries));
        } catch (Exception e) {
            logger.severe("Error analyzing email: " + e.getMessage());
            return CompletableFuture.completedFuture(new ExpenseModal.ExpenseResult(List.of()));
        }
    }

    private String detectAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String detectDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String detectMerchant(String text) {
        text = text.toLowerCase();
        if (text.contains("amazon")) return "Amazon";
        if (text.contains("uber")) return "Uber";
        if (text.contains("walmart")) return "Walmart";
        if (text.contains("chase")) return "Chase";
        return null;
    }
}

