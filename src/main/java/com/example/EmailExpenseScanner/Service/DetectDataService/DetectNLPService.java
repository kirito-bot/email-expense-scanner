package com.example.EmailExpenseScanner.Service.DetectDataService;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class DetectNLPService {

    private final StanfordCoreNLP pipeline;
    private static final Logger logger = Logger.getLogger(DetectNLPService.class.getName());

    private static final Set<String> RELATIVE_DATE_WORDS = new HashSet<>(Arrays.asList(
            "today", "tomorrow", "yesterday", "tonight", "now", "currently", "present",
            "next", "last", "previous", "upcoming"
    ));

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

    /**
     * Extracts all absolute dates from the text (ignores relative words like today, tomorrow).
     */
    public List<String> extractAllDates(String text) {
        List<String> dates = new ArrayList<>();
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        StringBuilder currentDate = new StringBuilder();

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.word();
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                if ("DATE".equalsIgnoreCase(ner) && !RELATIVE_DATE_WORDS.contains(word.toLowerCase())) {
                    if (!currentDate.isEmpty()) currentDate.append(" ");
                    currentDate.append(word);
                } else if (!currentDate.isEmpty()) {
                    dates.add(currentDate.toString().trim());
                    currentDate.setLength(0);
                }
            }

            if (!currentDate.isEmpty()) {
                dates.add(currentDate.toString().trim());
                currentDate.setLength(0);
            }
        }

        logger.info("Detected dates: " + dates);
        return dates.isEmpty() ? null : dates;
    }

    /**
     * Extracts all merchant/organization names from the text.
     */
    public List<String> extractAllMerchants(String text) {
        List<String> merchants = new ArrayList<>();
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
                } else if (!currentOrg.isEmpty()) {
                    merchants.add(currentOrg.toString().trim());
                    currentOrg.setLength(0);
                }
            }

            if (!currentOrg.isEmpty()) {
                merchants.add(currentOrg.toString().trim());
                currentOrg.setLength(0);
            }
        }

        logger.info("Detected merchants: " + merchants);
        return merchants.isEmpty() ? null : merchants;
    }

    /**
     * Extracts all money values from the text.
     */
    public List<String> extractAllMoney(String text) {
        List<String> moneyValues = new ArrayList<>();
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
                } else if (!currentMoney.isEmpty()) {
                    moneyValues.add(currentMoney.toString().trim());
                    currentMoney.setLength(0);
                }
            }

            if (!currentMoney.isEmpty()) {
                moneyValues.add(currentMoney.toString().trim());
                currentMoney.setLength(0);
            }
        }

        logger.info("Detected money values: " + moneyValues);
        return moneyValues.isEmpty() ? null : moneyValues;
    }
}
