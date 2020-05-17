package org.itmo.Components;

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


//аутентификация приветствие
@Component
public class TelegramUser {
    private Sheets sheetsService;
    private String APPLICATION_NAME = "Google Sheet";
    private String SPREADSHEET_ID = "1wOOgK2KK6OE7tmLPsJR-_Jt_sBVfCtD0Qk-n1CqZpbc";
    private String telegram_username = "@MarkStav";

    private String uploadPath = "/home/alex/work/java/Projects/TelegramBotVebinar/src/main/resources/";

    private boolean isUser = false;

    public TelegramUser(){}

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

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

    public Sheets getSheetsService() throws Exception {
        Credential credential = authorize();
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List value() throws Exception {
        sheetsService = getSheetsService();
        String range = "LeadsFromTilda!A2:H400";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();
        return values;
    }
    public boolean findUser(String username) {
        telegram_username = username;
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
                if (row.get(3).equals(telegram_username)){
                    isUser = true;
                    return true;
                }

            }
        }
        return false;
    }

    public String welcomeMessage() {
        return "Привет, " + telegram_username + "! Я бот помощник.\n" +
                " - буду держать вас в курсе всех  ивентов вебинара\n" +
                " - отправлю и проверю ваше дз\n" +
                " - если есть вопросы помогу связаться с администратором или спикером" +
                " - напомню ваш пароль";

    }

    public String negativeMessage(){
        return "Привет, вы еще не зарегистрировались на курс \n" +
                "Если вы регистрировались на курс, напишите нашему администратору @MarkStav";
    }

    public String returnPass() throws Exception {

        List<List<Object>> values = value();

        if (values == null || values.isEmpty()){
            System.out.println("No data found");
        } else {
            for (List row : values){
                if (row.get(3).equals(telegram_username)){
                    return (String)row.get(7);
                }

            }
        }
        return "Возникла ошибка, обратитесь к администратору @MarkStav";
    }
}
