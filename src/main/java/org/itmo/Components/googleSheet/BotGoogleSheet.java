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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BotGoogleSheet {

    @Value("${uploadPath}")
    private String uploadPath;

    @Value("${googleSheet}")
    private String SPREADSHEET_ID;

    @Value("${botAdmin}")
    private String botAdmin;

    private final String APPLICATION_NAME = "Google Sheet";


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

    private List mainTable() throws Exception {
        Sheets sheetsService = getSheetsService();
        String range = "LeadsFromTilda!A2:M400";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();
        return values;
    }

    private List adminTable() throws Exception {
        Sheets sheetsService = getSheetsService();
        String range = "Admin!A2:B400";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();
        return values;
    }

    private List cashTable() throws Exception {
        Sheets sheetsService = getSheetsService();
        String range = "Cash!A2:B400";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();
        return values;
    }

    private String correctUsername(String text){
        Pattern pattern = Pattern.compile("[A-Za-z0-9_]{1,}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String tx = text.substring(matcher.start(), matcher.end());

            return tx;
        }
        return text;
    }

    //Возвращае usernameSheet
    public Map<String, String> findUser(String username) {

        Map<String, String> userData = new HashMap<>();

        List<List<Object>> mainTableList = null;
        List<List<Object>> adminTableList = null;
        List<List<Object>> cashTableList = null;
        try {
            mainTableList = mainTable();
            adminTableList = adminTable();
            cashTableList = cashTable();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mainTableList == null || mainTableList.isEmpty()){
            System.out.println("No data found");
        } else {
            for (List row : mainTableList){
                String name = correctUsername((String) row.get(4));
                if (name.equals(username)){
                    userData.put("nameSheet", (String) row.get(0));

                    break;
                }

            }
        }

        if (adminTableList == null || adminTableList.isEmpty()){
            System.out.println("No data found");
        } else {
            for (List row : adminTableList){
                String name = correctUsername((String) row.get(0));
                if (name.equals(username)){
                    userData.put("role", "admin");
                    break;
                }

            }
        }
        return userData;
    }

    public String returnPass(String telegram_username)  {

        List<List<Object>> values = null;
        try {
            values = mainTable();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (values == null || values.isEmpty()){
            System.out.println("No data found");
        } else {
            for (List row : values){
                String name = correctUsername((String) row.get(4));

                if (name.equals(telegram_username)){
                    return (String)row.get(2);
                }

            }
        }
        return "Возникла ошибка, обратитесь к администратору " + botAdmin;
    }

    public String returnMail(String telegram_username) throws Exception {

        List<List<Object>> values = mainTable();

        if (values == null || values.isEmpty()){
            System.out.println("No data found");
        } else {
            for (List row : values){
                if (row.get(4).equals(telegram_username)){
                    return (String)row.get(1);
                }

            }
        }
        System.out.println("Возникла ошибка, обратитесь к администратору " + botAdmin);
        return null;
    }
}
