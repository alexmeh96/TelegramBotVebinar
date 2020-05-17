package org.itmo;

import org.itmo.Components.Greeting;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MainTelegramBot extends TelegramWebhookBot {
    private String webHookPath;
    private String botUserName;
    private String botToken;

    public  MainTelegramBot(DefaultBotOptions botOptions){
        super(botOptions);
    }
    public MainTelegramBot(){};


    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {

        if(update.getMessage().getText().equals("/start")){
            long chat_id = update.getMessage().getChatId();

            String username = update.getMessage().getFrom().getUserName();

            Greeting greeting = new Greeting(username);
            String message = greeting.helloMsg();

            try {
                execute(new SendMessage(chat_id, message));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }

        return null;
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
