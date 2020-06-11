package org.itmo.Components.model;

import com.google.api.services.drive.model.File;
import lombok.Getter;
import lombok.Setter;
import org.itmo.Components.googleDrive.GetSubFoldersByName;
import org.itmo.Components.googleDrive.TelegramBotGoogleDrive;
import org.itmo.Components.service.BotMessage;
import org.itmo.Components.service.TelegramButton;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.MainTelegramBot;
import org.itmo.config.BotProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * класс содержащий всх пользователе бота
 */
@Getter
@Setter
@Component
public class TelegramUsers {

    @Autowired
    TelegramBotGoogleDrive telegramBotGoogleDrive;
    @Autowired
    BotGoogleSheet botGoogleSheet;



    @Autowired
    public TelegramUsers(){
        try {
            deployUserSheet();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        System.out.println("dfa");

    }



    private Map<String, Date> mapDate = new HashMap<>();  //map номера домышки и даты её рассылки админом (key: номер дз, value: дата рассылки дз админом)

    private Map<String, Date> mapDateOther = new HashMap<>();  //map номера дополнительной домышки и даты её рассылки админом (key: номер дз, value: дата рассылки дз админом)

    private Map<String, User> userMap= new HashMap<>();    //map студентов  (key: username, value: студент)

    private Map<String, Admin> adminMap= new HashMap<>();    //map админов   (key: username, value: админ)




    /**
     * обновление пользовательских данных с таблицы
     * @param mainTelegramBot  //главный класс бота
     * @param chatId  админский чат, с которого было запущенно обновление
     * @throws TelegramApiException
     */
    public void update(MainTelegramBot mainTelegramBot, Long chatId) throws TelegramApiException {
        mainTelegramBot.execute(new SendMessage().setChatId(chatId).setText("Обновление студентов началось!"));
        try {
            List<List<Object>> lists = BotGoogleSheet.Reader();
            System.out.println(lists);

            for (int i = 0; i < lists.size() ; i++) {
                String username = BotGoogleSheet.correctUsername( (String) lists.get(i).get(BotProperty.SHEET_USERNAME_COL));
                if (lists.get(i).get(BotProperty.SHEET_ROLE_COL).equals("1") && userMap.containsKey(username)){

                    adminMap.put(username, new Admin(username, userMap.get(username).getChatId()));
                    userMap.remove(username);

                    SendMessage sendMessage = TelegramButton.adminMenu();
                    sendMessage.setChatId(adminMap.get(username).getChatId());
                    mainTelegramBot.execute(sendMessage);
                    continue;
                }
                if (lists.get(i).get(BotProperty.SHEET_ROLE_COL).equals("0") && adminMap.containsKey(username)){

                    Long chat = adminMap.get(username).getChatId();
                    String usernameSheet = (String) lists.get(i).get(BotProperty.SHEET_NAME_COL);
                    String rowId = String.valueOf(i+2);
                    File folderDirectory = telegramBotGoogleDrive.activate(usernameSheet);
                    User user = new User(chat,username, usernameSheet, folderDirectory, rowId);
                    user.setCash( Integer.valueOf((String)lists.get(i).get(BotProperty.SHEET_CASH_COL)) );
                    user.setVip( (String)lists.get(i).get(BotProperty.SHEET_TARIF_COL) );
                    userMap.put(username, user);
                    adminMap.remove(username);

                    SendMessage sendMessage = TelegramButton.userMenu(BotMessage.welcomeMessage(usernameSheet));
                    sendMessage.setChatId(chat);
                    mainTelegramBot.execute(sendMessage);
                    continue;
                }

                if (lists.get(i).get(BotProperty.SHEET_ROLE_COL).equals("0") && userMap.containsKey(username)){
                    userMap.get(username).setCash(  Integer.valueOf((String)lists.get(i).get(BotProperty.SHEET_CASH_COL)) );
                    userMap.get(username).setVip(  (String)lists.get(i).get(BotProperty.SHEET_TARIF_COL) );
                    System.out.println(userMap);
                }

            }

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            mainTelegramBot.execute(new SendMessage().setChatId(chatId).setText("Обновление студентов прошло неудачно!"));
            return;
        }
        mainTelegramBot.execute(new SendMessage().setChatId(chatId).setText("Обновление студентов прошло успешно!"));

    }


    public void deployUserSheet() throws IOException, GeneralSecurityException {

        BufferedReader br1 = new BufferedReader(new FileReader("src/main/resources/dateHW.txt"));
        String s = "";
        int k=1;
        while((s=br1.readLine())!=null){
            Date date = new Date(Long.parseLong(s));
            mapDate.put(Integer.toString(k), date);
            k++;
        }

        br1.close();

        BufferedReader br2 = new BufferedReader(new FileReader("src/main/resources/dateOtherHW.txt"));

        k=1;
        while((s=br2.readLine())!=null){
            Date date = new Date(Long.parseLong(s));
            mapDateOther.put(Integer.toString(k), date);
            k++;
        }
        br2.close();

        List<List<Object>> lists = BotGoogleSheet.Reader();


        if(lists==null || lists.isEmpty()) return;

        BotProperty.ID_ROW = lists.size()+2;

        File projectFolder = TelegramBotGoogleDrive.findFolder("Проект 1", null);
        File studentsFolderHW = TelegramBotGoogleDrive.findFolder("Домашнее задание", projectFolder);

        for (int i = 0; i < lists.size() ; i++) {
            String username = BotGoogleSheet.correctUsername((String) lists.get(i).get(BotProperty.SHEET_USERNAME_COL));

            if (lists.get(i).get(BotProperty.SHEET_ROLE_COL).equals("1")){
                Long chatId = Long.valueOf((String)lists.get(i).get(BotProperty.SHEET_CHAT_ID_COL));
                adminMap.put(username, new Admin(username, chatId));
                continue;
            }
            if (lists.get(i).get(BotProperty.SHEET_ROLE_COL).equals("0") ){

                String usernameSheet = (String) lists.get(i).get(BotProperty.SHEET_NAME_COL);
                String rowId = String.valueOf(i+2);
                com.google.api.services.drive.model.File folderDirectory = TelegramBotGoogleDrive.findFolder(usernameSheet, studentsFolderHW);



                Long chatId = Long.valueOf((String)lists.get(i).get(BotProperty.SHEET_CHAT_ID_COL));
                User user = new User(chatId, username, usernameSheet, folderDirectory, rowId);
                user.setCash( Integer.valueOf((String)lists.get(i).get(BotProperty.SHEET_CASH_COL)) );
                user.setVip( (String)lists.get(i).get(BotProperty.SHEET_TARIF_COL) );

                for (int j = 1; j <= 4; j++) {
                    String s1 = "Домашнее задание " + j;
                    if(TelegramBotGoogleDrive.findFolder(s1, folderDirectory)!=null){
                        user.getSendHW().add(String.valueOf(j));
                    }
                }

                if (user.getVip().equals("1")){
                    for (int j = 1; j <= 16; j++) {
                        String s1 = "#отчет" + j;
                        if(TelegramBotGoogleDrive.findFolder(s1, folderDirectory)!=null){
                            user.getSendOtherHW().add(String.valueOf(j));
                        }
                    }
                }


                userMap.put(username, user);
                System.out.println(user.getSendHW());
                continue;
            }



        }
        System.out.println(userMap);
        System.out.println(adminMap);

    }

}
