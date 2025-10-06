package com.example.email_expense_scanner;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.Collections;

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

            GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/gmail.readonly"));


            return new Gmail.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName(APPLICATION_NAME).build();
        }


}
