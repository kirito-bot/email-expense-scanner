package com.example.email_expense_scanner;


import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class GmailUtilsService {
    private GmailUtilsService() {
    }

    /**
     * Extracts the email body (plain text) from a Gmail Message, handling nested multiparts
     */
    public  String getEmailBody(Message message) {
        if (message == null || message.getPayload() == null) return null;
        return getHtmlBodyFromPart(message.getPayload());
    }

    private static String getHtmlBodyFromPart(MessagePart part) {
        if (part == null) return null;

        // 1️⃣ If body exists and MIME type is text/html, return it raw
        if (part.getBody() != null && part.getBody().getData() != null) {
            String decoded = new String(Base64.getUrlDecoder().decode(part.getBody().getData()));
            if ("text/html".equalsIgnoreCase(part.getMimeType())) {
                return decoded;
            }
        }

        // 2️⃣ If multipart, check nested parts
        if (part.getParts() != null) {
            for (MessagePart subPart : part.getParts()) {
                String result = getHtmlBodyFromPart(subPart);
                if (result != null && !result.isEmpty()) return result;
            }
        }

        return null;
    }


    /**
     * Extracts a specific header value (e.g., Subject, From) from a Gmail Message
     */
    public String getHeader(Message message, String headerName) {
        if (message == null || message.getPayload() == null) return null;

        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for (MessagePartHeader h : headers) {
            if (headerName.equalsIgnoreCase(h.getName())) {
                return h.getValue();
            }
        }
        return null;
    }

    /**
     * Parses an HTML table to extract  data (e.g., Merchant, Amount)
     * @param html
     */
    public void parseExpenseTable(String html) {
        Document doc = Jsoup.parse(html);

        // Select all tables (or a specific table by class/id)
        Elements tables = doc.select("table");

        for (Element table : tables) {
            Elements rows = table.select("tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() >= 2) { // example: 2 columns: Merchant | Amount
                    String merchant = cols.get(0).text();
                    String amount = cols.get(1).text();
                    System.out.println("Merchant: " + merchant + " | Amount: " + amount);
                }
            }
        }
    }
}


