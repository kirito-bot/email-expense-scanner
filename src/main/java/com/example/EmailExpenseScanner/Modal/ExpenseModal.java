package com.example.EmailExpenseScanner.Modal;

import java.util.List;

public class ExpenseModal {
    public record ExpenseEntry(List<String> merchant, List<String> amount, List<String> date, String messageNumber,
                               String message) {
    }

}
