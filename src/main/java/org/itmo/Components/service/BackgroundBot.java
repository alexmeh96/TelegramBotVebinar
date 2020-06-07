package org.itmo.Components.service;

import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.itmo.MainTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class BackgroundBot {

    @Autowired
    TelegramUsers telegramUsers;
    @Autowired
    MainTelegramBot mainTelegramBot;

    @Scheduled(cron="* */4 * * * *", zone="Europe/Moscow")
    public void doScheduledWork() {
        System.out.println("method");

        for (String num : telegramUsers.getMapDate().keySet()){
            for (User user : telegramUsers.getUserMap().values()){
                if (!user.getSendHW().contains(num)){

                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(user.getChatId());
                    sendMessage.setText("Вы ещё не сделали домашнее задание " + num + "!");
                    try {
                        mainTelegramBot.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for (String num : telegramUsers.getMapDateOther().keySet()){
            for (User user : telegramUsers.getUserMap().values()){
                if ( !user.getSendOtherHW().contains(num)){

                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(user.getChatId());
                    sendMessage.setText("Вы ещё не сделали дополнительное домашнее задание " + num + "!");
                    try {
                        mainTelegramBot.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
