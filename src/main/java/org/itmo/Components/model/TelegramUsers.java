package org.itmo.Components.model;

import lombok.Getter;
import lombok.Setter;
import org.itmo.Components.botButton.TelegramButton;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.MainTelegramBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Getter
@Setter
@Component
public class TelegramUsers {

    private Map<String, Date> mapDate = new HashMap<>();

    private Map<String, User> userMap= new HashMap<>();    //map студентов

    private Map<String, Admin> adminMap= new HashMap<>();    //map админов

    public void update(MainTelegramBot mainTelegramBot, Long chatId) throws TelegramApiException {
        mainTelegramBot.execute(new SendMessage().setChatId(chatId).setText("Обновление студентов началось!"));
        try {
            
            List<List<Object>> lists = BotGoogleSheet.Reader();

            for (User user: userMap.values()){
                int rowId = Integer.parseInt(user.getRowId())-2;

                user.setCash(  Integer.valueOf((String)lists.get(rowId).get(6)) );

                if ( Integer.parseInt((String)lists.get(rowId).get(5)) == 1){
                    adminMap.put(user.getUsername(), new Admin(user.getUsername(),user.getChatId()));
                }
            }

            SendMessage sendMessage = TelegramButton.createAdminMenu();

            for (Admin admin : adminMap.values()){
                userMap.remove(admin.getName());
                sendMessage.setChatId(admin.getChatId());
                mainTelegramBot.execute(sendMessage);
            }

        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            mainTelegramBot.execute(new SendMessage().setChatId(chatId).setText("Обновление студентов прошло неудачно!"));
            return;
        }
        mainTelegramBot.execute(new SendMessage().setChatId(chatId).setText("Обновление студентов прошло успешно!"));

    }

}
