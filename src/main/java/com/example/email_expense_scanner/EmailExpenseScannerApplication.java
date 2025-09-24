package com.example.email_expense_scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

@SpringBootApplication
public class EmailExpenseScannerApplication {
	private static final Logger logger = Logger.getLogger(EmailExpenseScannerApplication.class.getName());
	public static void main(String[] args) {

		SpringApplication.run(EmailExpenseScannerApplication.class, args);
		logger.info("Email Expense Scanner Application started successfully.");
	}

}
