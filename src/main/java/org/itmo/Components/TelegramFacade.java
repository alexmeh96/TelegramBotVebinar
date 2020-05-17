package org.itmo.Components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

//обработка сообщения
@Component
public class TelegramFacade {

    private BotState botState;

    @Autowired
    TelegramUser telegramUser;


    public SendMessage createAnswer(Update update){
        String message = update.getMessage().getText();

        switch (message){
            case "/start":
                botState = BotState.START;
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
                    String message = telegramUser.welcomeMessage();
                    MainMenu mainMenu = new MainMenu();
                    sendMessage = mainMenu.getMainMenuMessage(chat_id, message);
                }
                else {
                    sendMessage.setChatId(chat_id);
                    sendMessage.setText(telegramUser.negativeMessage());
                }
                break;
            case ANOTHER:
                sendMessage.setChatId(chat_id);
                sendMessage.setText("Моя, твоя, не понимать!");
                // sendMessage.setText(update.toString());

                //sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
                break;
        }


        return sendMessage;
    }

}
