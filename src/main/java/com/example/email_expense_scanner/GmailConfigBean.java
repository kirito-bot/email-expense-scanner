package com.example.email_expense_scanner;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Configuration
public class GmailConfigBean {

    private static final String APPLICATION_NAME = "email-expense-scanner";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE = "/client_secret.json";

    @Bean
    public Gmail gmailService() throws Exception {
        InputStream in = GmailConfigBean.class.getResourceAsStream(CREDENTIALS_FILE);
        if (in == null) {
            throw new RuntimeException("Missing " + CREDENTIALS_FILE);
        }

        GoogleClientSecrets googleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                googleClientSecrets,
                List.of(GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_LABELS)
        ).setAccessType("offline").build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(googleAuthorizationCodeFlow, receiver).authorize("user");

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential
        ).setApplicationName(APPLICATION_NAME).build();
    }


}
