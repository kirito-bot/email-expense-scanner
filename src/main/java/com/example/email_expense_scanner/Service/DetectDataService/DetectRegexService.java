package com.example.email_expense_scanner.Service.DetectDataService;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DetectRegexService {
    private static final Logger logger = Logger.getLogger(DetectRegexService.class.getName());
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\$\\d+(\\.\\d{1,2})?");
    private static final List<Pattern> DATE_PATTERN = Arrays.asList(
            Pattern.compile("\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b"),       // e.g., 10/05/2024
            Pattern.compile("\\b\\d{4}-\\d{1,2}-\\d{1,2}\\b"),         // e.g., 2025-08-02
            Pattern.compile("\\b\\d{1,2}\\s+\\w{3,9}\\s+\\d{4}\\b"),   // e.g., 2 Aug 2025
            Pattern.compile("\\b\\w{3,9}\\s+\\d{1,2},\\s+\\d{4}\\b")   // e.g., Aug 2, 2025
    );

    public String detectAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        String amount = matcher.find() ? matcher.group() : null;
        logger.info("Detected amount: " + amount + " in text: " + text);
        return amount;
    }

    public String detectDate(String text) {
        for (Pattern datePattern : DATE_PATTERN) {
            Matcher matcher = datePattern.matcher(text);
            if (matcher.find()) {
                String date = matcher.group();
                logger.info("Detected date: " + date + " in text: " + text);
                return date; // return only when a match is found
            }
        }
        logger.info("No date detected in text: " + text);
        return null; // no date matched any pattern
    }


    public String detectMerchant(String text) {

        List<String> knownMerchants = Arrays.asList("Amazon", "Uber", "Starbucks", "Walmart", "Netflix", "Apple", "Google", "Microsoft", "Target", "Costco",
                "Best Buy", "Home Depot", "eBay", "Chase", "Bank of America", "Citi", "Delta", "American Airlines", "Expedia", "Booking.com", "Airbnb", "Circle K",
                "Shell", "Exxon", "7-Eleven", "CVS", "Walgreens", "McDonald's", "Burger King", "Subway", "Domino's", "Pizza Hut", "KFC",
                "Taco Bell", "Chipotle", "Panera Bread", "Dunkin'", "Whole Foods", "Trader Joe's", "Costco", "Lowe's", "IKEA", "Sephora", "Ulta", "H&M",
                "Zara", "Nike", "Adidas", "Under Armour", "Gap", "Old Navy", "Macy's", "Nordstrom", "Bloomingdale's");
        text = text.toLowerCase();
        for (String merchant : knownMerchants) {
            if (text.contains(merchant.toLowerCase())) {
                logger.info("Detected merchant: " + merchant + " in text: " + text);
                return merchant;
            }
        }
        logger.info("No merchant detected in text: " + text);
        return "";
    }
}
