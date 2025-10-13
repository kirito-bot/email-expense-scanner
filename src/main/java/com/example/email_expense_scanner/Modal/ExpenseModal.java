package com.example.email_expense_scanner.Modal;

import java.util.List;

public class ExpenseModal {
    public record ExpenseEntry(List<String> merchant, List<String> amount, List<String> date, int messageNumber) {
    }

}
