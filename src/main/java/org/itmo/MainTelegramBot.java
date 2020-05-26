package org.itmo;

import org.itmo.Components.TelegramFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainTelegramBot extends TelegramWebhookBot {
    private String webHookPath;
    private String botUserName;
    private String botToken;

    @Autowired
    private TelegramFacade telegramFacade;

    public MainTelegramBot(DefaultBotOptions botOptions){
        super(botOptions);
    }

    public MainTelegramBot(){};


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {


        SendMessage sendMessage = null;
        try {
            sendMessage = telegramFacade.createAnswer(update);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return sendMessage;

    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }

    public void setWebHookPath(String webHookPath) {
        this.webHookPath = webHookPath;
    }

    public void setBotUserName(String botUserName) {
        this.botUserName = botUserName;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }
}
