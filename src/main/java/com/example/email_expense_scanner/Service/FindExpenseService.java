package com.example.email_expense_scanner.Service;

import com.example.email_expense_scanner.Modal.ExpenseModal;
import com.example.email_expense_scanner.Service.DetectDataService.DetectNLPService;
import com.example.email_expense_scanner.Service.DetectDataService.DetectRegexService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class FindExpenseService {
    private static final Logger logger = Logger.getLogger(FindExpenseService.class.getName());
    private final DetectRegexService detectRegexService;
    private final DetectNLPService detectNLPService;

    public FindExpenseService(@Autowired DetectRegexService detectRegexService, @Autowired DetectNLPService detectNLPService) {
        this.detectNLPService = detectNLPService;
        this.detectRegexService = detectRegexService;
    }

    @Async
    public CompletableFuture<ExpenseModal.ExpenseResult> analyzeEmailAsync(String htmlBody) {
        try {
            Document doc = Jsoup.parse(htmlBody);
            List<ExpenseModal.ExpenseEntry> entries = new ArrayList<>();
            List<String> merchantList = new ArrayList<>();
            List<String> amountList = new ArrayList<>();
            List<String> dateList = new ArrayList<>();

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
                        merchantList.add(merchant);
                    }
                    if (amount != null) {
                        amountList.add(amount);
                    }
                    if (date != null) {
                        dateList.add(date);
                    }
                    logger.info(dateList.size() + " dates, " + merchantList.size() + " merchants, " + amountList.size() + " amounts found.");

                }
            }
            for (String date : dateList) {
                logger.info("Date found in email: " + date);
            }
            for (String merchant : merchantList) {
                logger.info("Merchant found in email: " + merchant);
            }
            for (String amount : amountList) {
                logger.info("Amount found in email: " + amount);
            }
            // Remove duplicates

            dateList = this.removeDuplicates(dateList);
            merchantList = this.removeDuplicates(merchantList);
            amountList = this.removeDuplicates(amountList);

//            for (String date : dateList) {
//                for (String merchant : merchantList) {
//                    for (String amount : amountList) {
//                        entries.add(new ExpenseModal.ExpenseEntry(merchant, amount, date));
//                    }
//                }
//            }

            if (!dateList.isEmpty() && !merchantList.isEmpty() && !amountList.isEmpty()) {
                entries.add(new ExpenseModal.ExpenseEntry(merchantList.getFirst(), amountList.getFirst(), dateList.getFirst()));
                logger.info("Expense entry created from first detected values.");
                logger.info(dateList.size() + " dates, " + merchantList.size() + " merchants, " + amountList.size() + " amounts found." + entries.size() + " entries created.");
            } else {
                logger.info("No expense entries found in email.");
                logger.info(dateList.size() + " dates, " + merchantList.size() + " merchants, " + amountList.size() + " amounts found.");
            }


            return CompletableFuture.completedFuture(new ExpenseModal.ExpenseResult(entries));
        } catch (Exception e) {

            logger.severe("Error analyzing email: " + e.getMessage() + "\n" + e);
            return CompletableFuture.completedFuture(new ExpenseModal.ExpenseResult(List.of()));
        }
    }

    private List<String> removeDuplicates(List<String> list) {
        return new ArrayList<>(new java.util.HashSet<>(list));
    }

    private String detectAmount(String text) {

        String regexAmount = detectRegexService.detectAmount(text);
        String nlpAmount = detectNLPService.extractMoney(text);
        if (regexAmount != null) {
            return nlpAmount;
        } else if (nlpAmount != null) {
            return regexAmount;
        } else
            return null;
    }

    private String detectDate(String text) {
        String regexDate = detectRegexService.detectDate(text);
        String nlpDate = detectNLPService.extractAbsoluteDate(text);

        if (regexDate != null) {
            return nlpDate;
        } else if (nlpDate != null) {
            return regexDate;
        } else
            return null;
    }

    private String detectMerchant(String text) {
        String regexMerchant = detectRegexService.detectMerchant(text);
        String nlpMerchant = detectNLPService.extractMerchant(text);
        if (regexMerchant != null) {
            return nlpMerchant;
        } else if (nlpMerchant != null) {
            return regexMerchant;
        } else
            return null;
    }
}

