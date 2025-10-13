package com.example.email_expense_scanner.Service;

import com.example.email_expense_scanner.Modal.ExpenseModal;
import com.example.email_expense_scanner.Service.DetectDataService.DetectNLPService;
import com.example.email_expense_scanner.Service.DetectDataService.DetectRegexService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
            String text = htmlBody;
            List<String> merchantList = detectMerchant(text);
            List<String> amountList = detectAmount(text);
            List<String> dateList = detectDate(text);

            logger.info("TOTAL found : " + dateList.size() + " dates, " + merchantList.size() + " merchants, " + amountList.size() + " amounts found.");

            for (String date1 : dateList) {
                logger.info("Date found in email: " + date1);
            }
            for (String merchant1 : merchantList) {
                logger.info("Merchant found in email: " + merchant1);
            }
            for (String amount1 : amountList) {
                logger.info("Amount found in email: " + amount1);
            }


//            if (!dateList.isEmpty() && !merchantList.isEmpty() && !amountList.isEmpty()) {
//                entries.add(new ExpenseModal.ExpenseEntry(merchantList.getFirst(), amountList.getFirst(), dateList.getFirst()));
//                logger.info("Expense entry created from first detected values.");
//                logger.info(dateList.size() + " dates, " + merchantList.size() + " merchants, " + amountList.size() + " amounts found." + entries.size() + " entries created.");
//            } else {
//                logger.info("No expense entries found in email.");
//                logger.info(dateList.size() + " dates, " + merchantList.size() + " merchants, " + amountList.size() + " amounts found.");
//            }
            return CompletableFuture.completedFuture(new ExpenseModal.ExpenseResult(entries));
        } catch (Exception e) {

            logger.severe("Error analyzing email: " + e.getMessage() + "\n" + e);
            return CompletableFuture.completedFuture(new ExpenseModal.ExpenseResult(List.of()));
        }
    }

    private List<String> combineListAndRemoveDuplicates(List<String> list1, List<String> list2) {
        Set<String> mergeSet = new java.util.HashSet<>(list1);
        mergeSet.addAll(list2);
        return new ArrayList<>(mergeSet);
    }

    private List<String> detectAmount(String text) {

        List<String> regexAmount = detectRegexService.detectAmount(text);
        List<String> nlpAmount = detectNLPService.extractAllMoney(text);
        return combineListAndRemoveDuplicates(regexAmount, nlpAmount);
    }

    private List<String> detectDate(String text) {
        List<String> regexDate = detectRegexService.detectDate(text);
        List<String> nlpDate = detectNLPService.extractAllDates(text);
        return combineListAndRemoveDuplicates(regexDate, nlpDate);
    }

    private List<String> detectMerchant(String text) {
        List<String> regexMerchant = detectRegexService.detectMerchant(text);
        List<String> nlpMerchant = detectNLPService.extractAllMerchants(text);
        return combineListAndRemoveDuplicates(regexMerchant, nlpMerchant);
    }
}

