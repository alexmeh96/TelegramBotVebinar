package org.itmo.Components.googleSheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.slf4j.Slf4j;
import org.itmo.config.BotProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
@Slf4j
@Component
public class BotGoogleSheet {
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(BotGoogleSheet.class);

    private String uploadPath = BotProperty.PATH + "/resources/";

    private static final String APPLICATION_NAME = "Google Sheet";

    final private int idRow = 2;

    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

//    public BotGoogleSheet(){
//        sheetsService = getSheetsService();
//    }
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

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

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = BotGoogleSheet.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static Sheets ServiceApp() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    public String makeRange(int idRow){
        String range = "Full!A" + idRow + ":H" + idRow;
        return range;
    }


    private List mainTable()  {

        String range = "LeadsFromTilda!A2:M400";

        ValueRange response = null;
        try {
            response = ServiceApp().spreadsheets().values()
                    .get(BotProperty.SPREADSHEET_ID, range)
                    .execute();
            log.info("Получена основная таблица пользователей");
        } catch (IOException | GeneralSecurityException e) {
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




    //Возвращае usernameSheet
    public Map<String, String> findUser(String username) {

        Map<String, String> userData = new HashMap<>();

        List<List<Object>> mainTableList = null;
        try {
            mainTableList = mainTable();
            log.info("Заполнение mainTableList и cashTableList");

        } catch (Exception e) {
            log.trace("Ошибка заполнения mainTableList и cashTableList: {}", e.getStackTrace());
        }

        if (mainTableList == null || mainTableList.isEmpty()){
            log.error("Основная таблица пустая!");
        } else {
            int indexRow = idRow;
            for (List row : mainTableList) {
                String name = correctUsername((String) row.get(4));
                if (name.equals(username)){

                    try {
                        List<List<Object>> list = new ArrayList();
                        list.add(row.subList(0, 5));
                        indexRow = addWriter(indexRow, correct_num(list));
                        userData.put("nameSheet", (String) row.get(0));
                        userData.put("row", String.valueOf(indexRow));

                    } catch (IOException|GeneralSecurityException e) {
                        e.printStackTrace();
                    }

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

    /**
     * Метод исправляющий вид номеров в таблицы для корректного отображения
     * @param values Список списков объектов с данными о пользователях
     * @return Обработанный список
     */
    public static List<List<Object>> correct_num(List<List<Object>> values){
        for (List<Object> row: values) {
            String num = "\'" + (String) row.get(3);
            row.set(3, num);
        }
        return values;
    }

    /**
     * Метод зануляющий столбцы
     * @param values Список списков объектов с данными о пользователях
     * @return Обработанный список
     */
    public static List<List<Object>> null_columns(List<List<Object>> values){
        System.out.println(values);
        for (List<Object> row: values) {
            row.add(5, 0);
            row.add(6, 0);
            row.add(7, 0);
        }
        return values;
    }

    /**
     * Метод считывающий данные из гугл таблицы
     * @param range Диапозон требуемый для считывания
     * @return Обработанный список
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static List<List<Object>> Reader() throws IOException, GeneralSecurityException {
        String range = "Full!A2:H400";
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values().get(BotProperty.SPREADSHEET_ID, range).execute();

        List<List<Object>> values = response.getValues();

        return values;

    }

    /**
     * Метод записывающий данные в гугл таблицу
     * @param range Диапозон требуемый для считывания
     * @param values Список списков объектов с данными о пользователях
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void Update(int idColumn, String row, String value) throws IOException, GeneralSecurityException {
        String column = correct_column(idColumn);
        String range = "Full!"+column + row;
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        List<List<Object>> objectList = new ArrayList<>();
        objectList.add(new ArrayList<>());
        objectList.get(0).add(value);

        ValueRange appendBody = new ValueRange()
                .setValues(objectList);
        UpdateValuesResponse appendResult = service.spreadsheets().values()
                .update(BotProperty.SPREADSHEET_ID, range, appendBody)
                .setValueInputOption("RAW")
                .execute();
    }


    public int addWriter(int idRow, List<List<Object>> values) throws IOException, GeneralSecurityException {
        System.out.println(values);
        String range = makeRange(idRow);
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ValueRange appendBody = new ValueRange()
                .setValues(null_columns(values));
        AppendValuesResponse appendResult = service.spreadsheets().values()
                .append(BotProperty.SPREADSHEET_ID, range, appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
        idRow++;
        return idRow;
    }

    private static String correct_column(int id){
        String column;
        List<Character> symbol = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H');
        column = String.valueOf(symbol.get(id));
        return column;
    }

}
