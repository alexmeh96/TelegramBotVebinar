package org.itmo.Components.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.itmo.MainTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
@Getter
@Setter
public class BackgroundBot {

    @Autowired
    TelegramUsers telegramUsers;
    @Autowired
    MainTelegramBot mainTelegramBot;

    @Value("${pingtask.url}")
    private String url;

    @Scheduled(cron = "0 0 12 * * *", zone = "Europe/Moscow")
    public void doScheduledWork() {

        for (String num : telegramUsers.getMapDate().keySet()) {
            for (User user : telegramUsers.getUserMap().values()) {
                if (!user.getSendHW().contains(num)) {
                    if (!user.getFailedHW().containsKey(num)) {
                        user.getFailedHW().put(num, 0);
                    }
                    if (user.getFailedHW().get(num) < 3) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(user.getChatId());
                        sendMessage.setText("Вы ещё не сделали домашнее задание " + num + "‼️😡");
                        try {
                            mainTelegramBot.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        user.getFailedHW().put(num, user.getFailedHW().get(num) + 1);
                    }
                }
            }
        }

        for (String num : telegramUsers.getMapDateOther().keySet()) {
            for (User user : telegramUsers.getUserMap().values()) {
                if (!user.getSendOtherHW().contains(num)) {
                    if (!user.getFailedOtherHW().containsKey(num)) {
                        user.getFailedOtherHW().put(num, 0);
                    }
                    if (user.getFailedOtherHW().get(num) < 3) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(user.getChatId());
                        sendMessage.setText("Вы ещё не сделали дополнительное домашнее задание " + num + "‼️😡");
                        try {
                            mainTelegramBot.execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        user.getFailedOtherHW().put(num, user.getFailedOtherHW().get(num) + 1);
                    }
                }
            }
        }

    }


    @Scheduled(fixedRateString = "${pingtask.period}")
    public void pingMe() {
        try {
            URL url = new URL(getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
