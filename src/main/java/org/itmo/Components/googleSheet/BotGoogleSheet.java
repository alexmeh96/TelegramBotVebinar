package org.itmo.Components.googleSheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Component
public class BotGoogleSheet {

    @Value("${uploadPath}")
    private String uploadPath;

    private final String APPLICATION_NAME = "Google Sheet";
    private final String SPREADSHEET_ID = "1wOOgK2KK6OE7tmLPsJR-_Jt_sBVfCtD0Qk-n1CqZpbc";


    private Credential authorize() throws Exception {
//        InputStream in = Main.class.getResourceAsStream("credentials.json");
        InputStream in = new FileInputStream(uploadPath + "credentials.json");

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), new InputStreamReader(in)
        );
        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver())
                .authorize("user");
        return credential;
    }

    private Sheets getSheetsService() throws Exception {
        Credential credential = authorize();
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private List value() throws Exception {
        Sheets sheetsService = getSheetsService();
        String range = "LeadsFromTilda!A2:E400";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();
        return values;
    }

    //Возвращае usernameSheet
    public String findUser(String username) {

        List<List<Object>> values = null;
        try {
            values = value();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (values == null || values.isEmpty()){
            System.out.println("No data found");
        } else {
            for (List row : values){
                if (row.get(4).equals(username)){

                    String username_sheets = (String) row.get(0);

                    return username_sheets;
                }

            }
        }
        return null;
    }

    public String returnPass(String telegram_username) throws Exception {

        List<List<Object>> values = value();

        if (values == null || values.isEmpty()){
            System.out.println("No data found");
        } else {
            for (List row : values){
                if (row.get(4).equals(telegram_username)){
                    return (String)row.get(2);
                }

            }
        }
        return "Возникла ошибка, обратитесь к администратору @MarkStav";
    }

    public String returnMail(String telegram_username) throws Exception {

        List<List<Object>> values = value();

        if (values == null || values.isEmpty()){
            System.out.println("No data found");
        } else {
            for (List row : values){
                if (row.get(4).equals(telegram_username)){
                    return (String)row.get(1);
                }

            }
        }
        return "Возникла ошибка, обратитесь к администратору @MarkStav";
    }
}
