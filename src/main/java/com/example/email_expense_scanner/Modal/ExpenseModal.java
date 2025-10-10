package com.example.email_expense_scanner.Modal;

import java.util.List;

public class ExpenseModal {
    public record ExpenseEntry(String merchant, String amount, String date) {}

    public record ExpenseResult(List<ExpenseEntry> entries) {}
}
