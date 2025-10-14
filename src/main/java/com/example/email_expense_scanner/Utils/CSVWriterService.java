package com.example.email_expense_scanner.Utils;

import com.example.email_expense_scanner.Modal.ExpenseModal;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

@Service
public class CSVWriterService {

    private static final Logger logger = Logger.getLogger(CSVWriterService.class.getName());
    private final String CSV_FILE_PATH;

    public CSVWriterService() {
        String timestamp = DateTime.now(DateTimeZone.forID("America/New_York"))
                .toString("yyyy-MM-dd-HH-mm-ss");
        this.CSV_FILE_PATH = "C:\\logs\\csv\\output-" + timestamp + ".csv";

    }

    public void writeToCSV(List<ExpenseModal.ExpenseEntry> data, int batch) {
        File file = new File(CSV_FILE_PATH);
        boolean fileExists = file.getParentFile().mkdirs(); // ensure directory exists

        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            if (!fileExists || file.length() == 0) {
                out.println("Merchant,Date,Amount");
            }
            for (ExpenseModal.ExpenseEntry entry : data) {
                String row = String.format("%s,%s,%s",
                        entry.merchant(),
                        entry.date(),
                        entry.amount());
                out.println(row);
            }

            logger.info("Data successfully written to CSV: " + CSV_FILE_PATH +
                    " with " + data.size() + " entries. For batch number: " + batch);

        } catch (IOException e) {
            logger.severe("Failed to write to CSV file: " + e.getMessage() + e);
        }
    }

}
