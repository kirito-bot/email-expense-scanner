package com.example.EmailExpenseScanner.Service;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

@Service
public class TransactionTrainerService {

    private static final Logger logger = Logger.getLogger(TransactionTrainerService.class.getName());
    private MultiLayerNetwork model;
    private final int inputSize = 128;
    private final Path modelDir = Path.of("model");
    private final Path latestModelPath = modelDir.resolve("transaction-model-latest.zip");

    public TransactionTrainerService() throws Exception {
        Files.createDirectories(modelDir);
        if (Files.exists(latestModelPath)) {
            model = MultiLayerNetwork.load(latestModelPath.toFile(), true);
            logger.info("Loaded latest model from " + latestModelPath);
        } else {
            model = buildModel();
            logger.info("Created new model");
        }
    }

    // ---------- TRAIN MODEL ----------
    public void train(List<String> texts, List<Integer> labels) throws Exception {
        if (texts.size() != labels.size())
            throw new IllegalArgumentException("Texts and labels must match");

        double[][] features = new double[texts.size()][inputSize];
        double[][] labelsArray = new double[texts.size()][1];

        for (int i = 0; i < texts.size(); i++) {
            double[] vec = vectorize(texts.get(i));
            System.arraycopy(vec, 0, features[i], 0, inputSize);
            labelsArray[i][0] = labels.get(i);
        }

        INDArray X = Nd4j.create(features);
        INDArray Y = Nd4j.create(labelsArray);

        for (int epoch = 0; epoch < 5; epoch++) {
            model.fit(X, Y);
        }

        saveModel();
        logger.info("Model trained and version saved.");
    }

    // ---------- CLASSIFY ----------
    public boolean isTransaction(String text) {
        double[] vec = vectorize(text);
        INDArray input = Nd4j.create(new double[][]{vec});
        double prob = model.output(input).getDouble(0, 0);
        return prob > 0.5;
    }

    // ---------- CONTINUOUS FEEDBACK ----------
    public void addFeedback(String text, int label) throws Exception {
        double[] vec = vectorize(text);
        INDArray X = Nd4j.create(new double[][]{vec});
        INDArray Y = Nd4j.create(new double[][]{{label}});
        model.fit(X, Y);
        saveModel();
        logger.info("Model updated with feedback and versioned.");
    }

    // ---------- HELPERS ----------
    private double[] vectorize(String text) {
        double[] vec = new double[inputSize];
        for (String token : text.toLowerCase().split("\\s+")) {
            int idx = Math.abs(token.hashCode()) % inputSize;
            vec[idx] += 1.0;
        }
        return vec;
    }

    private MultiLayerNetwork buildModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .updater(new Adam(1e-3))
                .list()
                .layer(new DenseLayer.Builder().nIn(inputSize).nOut(64)
                        .activation(Activation.RELU).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .activation(Activation.SIGMOID).nIn(64).nOut(1).build())
                .build();
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        return net;
    }

    // ---------- SAVE MODEL WITH VERSIONING ----------
    public void saveModel() {
        try {
            // 1️⃣ Generate a timestamped version filename
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                    .format(LocalDateTime.now());
            String versionedFileName = "transaction-model-" + timestamp + ".zip";
            Path versionedPath = modelDir.resolve(versionedFileName);

            // 2️⃣ Save both versioned model and "latest"
            model.save(versionedPath.toFile(), true);
            model.save(latestModelPath.toFile(), true);

            logger.info("Model saved as version: " + versionedPath);
        } catch (Exception e) {
            logger.severe("Failed to save model: " + e.getMessage());
        }
    }
}
