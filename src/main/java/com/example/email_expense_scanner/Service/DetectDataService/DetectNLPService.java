package com.example.email_expense_scanner.Service.DetectDataService;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class DetectNLPService {

    private final StanfordCoreNLP pipeline;

    private static final Logger logger = Logger.getLogger(DetectNLPService.class.getName());

    public DetectNLPService() {
        try {
            Properties props = new Properties();
            props.setProperty("sutime.binders", "0");
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
            this.pipeline = new StanfordCoreNLP(props);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize StanfordCoreNLP", e);
        }
    }

    public String extractAbsoluteDate(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        StringBuilder nlpDate = new StringBuilder();

        // List of relative date keywords to ignore
        Set<String> relativeWords = new HashSet<>(Arrays.asList(
                "today", "tomorrow", "yesterday",
                "tonight", "now", "currently", "present",
                "next", "last", "previous", "upcoming"
        ));

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.word();
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if ("DATE".equalsIgnoreCase(ner)) {
                    if (relativeWords.contains(word.toLowerCase())) {
                        nlpDate.setLength(0);
                        continue;
                    }
                    if (!nlpDate.isEmpty()) nlpDate.append(" ");
                    nlpDate.append(word);
                } else if (!nlpDate.isEmpty()) {
                    // return the first accumulated NLP date
                    logger.info("Absolute date found in text: " + nlpDate.toString().trim());
                    return nlpDate.toString().trim();
                }
            }
            if (!nlpDate.isEmpty()) {
                logger.info("Absolute date found in text: " + nlpDate.toString().trim());
                return nlpDate.toString().trim();
            }
        }
        logger.info("No absolute date found in text: " + text);
        return null; // no absolute date found
    }

    /**
     * Extracts the first detected merchant/organization name.
     * Returns null if none found.
     */
    public String extractMerchant(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        StringBuilder currentOrg = new StringBuilder();

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.word();
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if ("ORGANIZATION".equalsIgnoreCase(ner)) {
                    if (!currentOrg.isEmpty()) currentOrg.append(" ");
                    currentOrg.append(word);
                } else {
                    if (!currentOrg.isEmpty()) {
                        String result = currentOrg.toString().trim();
                        if (!result.isEmpty()) return result;
                        currentOrg.setLength(0);
                    }
                }
            }

            if (!currentOrg.isEmpty()) {
                String result = currentOrg.toString().trim();
                if (!result.isEmpty()) {
                    logger.info("Merchant/organization found in text: " + result);
                    return result;
                }
            }
        }
        logger.info("No merchant/organization found in text: " + text);
        return null;
    }

    /**
     * Extracts and returns the first detected money value (e.g., "$50", "Rs. 200", "USD 100.25").
     * Returns null if none found.
     */
    public String extractMoney(String text) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        StringBuilder currentMoney = new StringBuilder();

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.word();
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if ("MONEY".equalsIgnoreCase(ner)) {
                    if (!currentMoney.isEmpty()) currentMoney.append(" ");
                    currentMoney.append(word);
                } else {
                    if (!currentMoney.isEmpty()) {
                        String result = currentMoney.toString().trim();
                        if (!result.isEmpty()) return result;
                        currentMoney.setLength(0);
                    }
                }
            }

            // Handle end-of-sentence case
            if (!currentMoney.isEmpty()) {
                String result = currentMoney.toString().trim();
                if (!result.isEmpty()) {
                    logger.info("Money value found in text: " + result);
                    return result;
                }
            }
        }
        logger.info("No money value found in text: " + text);
        return null; // No money value found
    }

}


