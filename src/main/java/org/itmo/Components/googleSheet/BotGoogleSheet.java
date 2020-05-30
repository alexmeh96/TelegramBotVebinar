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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
@Slf4j
@Component
public class BotGoogleSheet {
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(BotGoogleSheet.class);
    //@Value("${uploadPath}")
    private String uploadPath = "/home/alex/work/java/Projects/TelegramBotVebinar/src/main/resources/";

   // @Value("${googleSheet}")
    private String SPREADSHEET_ID = "1wOOgK2KK6OE7tmLPsJR-_Jt_sBVfCtD0Qk-n1CqZpbc";

    @Value("${botAdmin}")
    private String botAdmin;

    private final String APPLICATION_NAME = "Google Sheet";

    private Sheets sheetsService;

    public BotGoogleSheet(){
        sheetsService = getSheetsService();
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

    private Sheets getSheetsService(){
        Sheets sheets = null;
        try {
            Credential credential = authorize();
             sheets = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
             log.info("Прошла авторицация в Google Sheet");
        } catch (Exception e) {
            log.trace("Не прошла авторицация в Google Sheet: {}", e.getStackTrace());
//            e.printStackTrace();
        }

        return sheets;
    }

    private List mainTable()  {

        String range = "LeadsFromTilda!A2:M400";

        ValueRange response = null;
        try {
            response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();
            log.info("Получена основная таблица пользователей");
        } catch (IOException e) {
            log.trace("Вылетела птичка, ЛОВИ ИСКЛЮЧЕНИЕ! \n {}", e.getStackTrace());
//            e.printStackTrace();
        }
        List<List<Object>> values = response.getValues();
        return values;
    }

    private List adminTable(){
        String range = "Admin!A2:B400";

        ValueRange response = null;
        try {
            response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();
            log.info("Получена админская таблица пользователей");
        } catch (IOException e) {
            log.trace("Вылетела птичка, ЛОВИ ИСКЛЮЧЕНИЕ! \n {}", e.getStackTrace());
//            e.printStackTrace();
        }
        List<List<Object>> values = response.getValues();
        return values;
    }

    private List cashTable() {
        String range = "Cash!A2:B400";

        ValueRange response = null;
        try {
            response = sheetsService.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute();
            log.info("Получена таблица с монетами пользователей");
        } catch (IOException e) {
            log.trace("Вылетела птичка, ЛОВИ ИСКЛЮЧЕНИЕ! \n {}", e.getStackTrace());
//            e.printStackTrace();
        }

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

    public boolean findAdminTable(String username){
        List<List<Object>> adminTableList = null;
        try {
            adminTableList = adminTable();
            log.info("Заполнение adminTableList");
        } catch (Exception e) {
            log.trace("Ошибка заполнения adminTableList: {}", e.getStackTrace());
//            e.printStackTrace();
        }

        if (adminTableList == null || adminTableList.isEmpty()){
            log.error("Админская таблица пустая!");
//            System.out.println("No data found");
        } else {
            for (List row : adminTableList){
                String name = correctUsername((String) row.get(0));
                if (name.equals(username)){
                    log.info("{} - крутой админ!", username);
                    return true;
                }

            }
        }
        return false;
    }


    //Возвращае usernameSheet
    public Map<String, String> findUser(String username) {

        Map<String, String> userData = new HashMap<>();

        List<List<Object>> mainTableList = null;

        List<List<Object>> cashTableList = null;
        try {
            mainTableList = mainTable();
            cashTableList = cashTable();
            log.info("Заполнение mainTableList и cashTableList");

        } catch (Exception e) {
            log.trace("Ошибка заполнения mainTableList и cashTableList: {}", e.getStackTrace());
//            e.printStackTrace();
        }

        if (mainTableList == null || mainTableList.isEmpty()){
            log.error("Основная таблица пустая!");
        } else {
            for (List row : mainTableList){
                String name = correctUsername((String) row.get(4));
                if (name.equals(username)){
                    userData.put("nameSheet", (String) row.get(0));
                    log.info("Пополнение в userData");
                    break;
                }

            }
        }

        if (cashTableList == null || cashTableList.isEmpty()){
            log.error("Таблица с монетами пустая!");
        } else {
            for (List row : cashTableList){
                String name = correctUsername((String) row.get(0));
                if (name.equals(username)){
                    userData.put("cash", (String) row.get(1));
                    log.info("Пополнение в userData");
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
            log.info("Заполнение PassList");
        } catch (Exception e) {
            log.trace("Основная таблица пустая: {}", e.getStackTrace());
//            e.printStackTrace();
        }

        if (values == null || values.isEmpty()){
            log.error("Основная таблица пустая!");
        } else {
            for (List row : values){
                String name = correctUsername((String) row.get(4));

                if (name.equals(telegram_username)){
                    log.info("{} успешно запросил пароль", telegram_username);
                    return (String)row.get(2);
                }

            }
        }
        log.warn("Ошибка запроса пароля для {}", telegram_username);
        return null;
    }

    public String returnMail(String telegram_username) throws Exception {

        List<List<Object>> values = mainTable();

        if (values == null || values.isEmpty()){
            log.error("Основная таблица пустая!");
        } else {
            for (List row : values){
                if (row.get(4).equals(telegram_username)){
                    log.info("{} успешно запросил почту", telegram_username);
                    return (String)row.get(1);
                }

            }
        }
        log.warn("Ошибка запроса почты для {}", telegram_username);
        return null;
    }
}
