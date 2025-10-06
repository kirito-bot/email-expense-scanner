package com.example.email_expense_scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

@SpringBootApplication
public class EmailExpenseScannerApplication implements CommandLineRunner {
	private static final Logger logger = Logger.getLogger(EmailExpenseScannerApplication.class.getName());
	final GmailReaderService gmailReaderService;

	public EmailExpenseScannerApplication(GmailReaderService gmailReaderService) {
		this.gmailReaderService = gmailReaderService;
	}
	public static void main(String[] args) {
		SpringApplication.run(EmailExpenseScannerApplication.class, args);
		logger.info("Email Expense Scanner Application started successfully.");
	}

	@Override
	public void run(String... args) throws Exception {
		gmailReaderService.listUnreadMessages();
	}
}
