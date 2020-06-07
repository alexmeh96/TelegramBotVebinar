package org.itmo.Components.model;

import com.google.api.services.drive.model.File;
import lombok.Getter;
import lombok.Setter;
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
                String username = (String) lists.get(i).get(BotProperty.SHEET_USERNAME_COL);
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

}
