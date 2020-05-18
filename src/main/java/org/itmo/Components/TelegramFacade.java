package org.itmo.Components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

//обработка сообщения
@Component
public class TelegramFacade {

    private BotState botState;

    @Autowired
    TelegramUser telegramUser;
    @Autowired
    BotMessage botMessage;

    public SendMessage createAnswer(Update update){
        String message = update.getMessage().getText();

        switch (message){
            case "/start":
                botState = BotState.START;
                break;
            case "тех поддержка":
                botState = BotState.ASK_SUPPORT;
                break;
            case "админ":
                botState = BotState.ASK_ADMIN;
                break;
            case "спикер":
                botState = BotState.ASK_SPIKER;
                break;
            case "главное меню":
                botState = BotState.ASK_MAIN_MENU;
                break;
            case "отправить дз":
                botState = BotState.ASK_SEND_HOMEWORK;
                break;
            case "пароль от личного кабинета":
                botState = BotState.ASK_PASSWORD;
                break;
            default:
                botState = BotState.ANOTHER;
                break;
        }

        return createMessage(update, botState);

    }

    private SendMessage createMessage(Update update, BotState botState){
        SendMessage sendMessage = new SendMessage();
        long chat_id = update.getMessage().getChatId();

        switch (botState){
            case START:
                String username = update.getMessage().getFrom().getUserName();

                if(telegramUser.findUser(username)){
                //if(telegramUser.findUser("@MarkStav")){
                    String message = telegramUser.welcomeMessage();
                    MainMenu mainMenu = new MainMenu();
                    sendMessage = mainMenu.getMainMenuMessage(chat_id, message);
                }
                else {
                    sendMessage.setChatId(chat_id);
                    sendMessage.setText(telegramUser.negativeMessage());
                    sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
                }
                break;
            case ASK_SUPPORT:
                Support support = new Support();
                sendMessage = support.getSupportMessage(chat_id);
                break;
            case ASK_ADMIN:
                sendMessage.setChatId(chat_id);
                sendMessage.setText(botMessage.messageAdmin());
                break;
            case ASK_SPIKER:
                sendMessage.setChatId(chat_id);
                sendMessage.setText(botMessage.messageSpiker());
                break;
            case ASK_MAIN_MENU:
                MainMenu mainMenu = new MainMenu();
                sendMessage = mainMenu.getMainMenuMessage(chat_id, "меню");
                break;
            case ASK_SEND_HOMEWORK:
                sendMessage.setChatId(chat_id);
                sendMessage.setText("в разработке");
                break;
            case ASK_PASSWORD:
                sendMessage.setChatId(chat_id);
                sendMessage.setText("в разработке");
                break;
            case ANOTHER:
                sendMessage.setChatId(chat_id);
                sendMessage.setText("Моя, твоя, не понимать!");
                break;
        }


        return sendMessage;
    }

}
