package com.example.EmailExpenseScanner;


import com.example.EmailExpenseScanner.Service.TransactionTrainerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TransactionRunner implements CommandLineRunner {

    private final TransactionTrainerService trainer;

    public TransactionRunner(TransactionTrainerService trainer) {
        this.trainer = trainer;
    }

    @Override
    public void run(String... args) throws Exception {
        // Training only if first time
//        List<String> texts = List.of(
//                "You paid $300 to Circle K #300 on date 2025",
//                "Payment to Walmart $150 on 2024-12-01",
//                "Your verification code is 4532",
//                "Meeting scheduled for Monday"
//        );
//        List<Integer> labels = List.of(1,1,0,0);

//        trainer.train(texts, labels); // will overwrite only first time

        // Classify new text
        String text = "Paid $200 to Shell Gas Station on 2025-01-01";
        boolean isTx = trainer.isTransaction(text);
        System.out.println("Text: " + text);
        System.out.println("Is transaction? " + isTx);

        // Feedback example (fine-tune without retraining from scratch)
        trainer.addFeedback(text, (isTx) ? 1 : 0);
        trainer.saveModel();
    }
}
