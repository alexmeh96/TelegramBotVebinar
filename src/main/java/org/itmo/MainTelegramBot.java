package org.itmo;

import lombok.extern.slf4j.Slf4j;
import org.itmo.Components.TelegramFacade;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("ALL")
@Slf4j
public class MainTelegramBot extends TelegramWebhookBot {
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(MainTelegramBot.class);

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



        BotApiMethod<?> sendMessage = null;
        try {
            log.info("Получено сообщение");
            sendMessage = telegramFacade.createAnswer(update);
        } catch (Exception e) {
            //log.trace("Ошибка сборки сообщения: {}", e.getStackTrace());
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
