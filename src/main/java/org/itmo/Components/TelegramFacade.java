package org.itmo.Components;

import com.google.api.services.drive.model.File;
import org.itmo.Components.botButton.MainMenu;
import org.itmo.Components.botButton.Support;
import org.itmo.Components.botFile.TelegramBotFile;
import org.itmo.Components.googleDrive.TelegramBotGoogleDrive;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.Components.model.User;
import org.itmo.Components.model.UsersTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.InputStream;


//обработка сообщения
@Component
public class TelegramFacade {

    private BotState botState;

    @Autowired
    BotGoogleSheet botGoogleSheet;
    @Autowired
    BotMessage botMessage;
    @Autowired
    TelegramBotGoogleDrive telegramBotGoogleDrive;
    @Autowired
    UsersTelegramBot usersTelegramBot;
    @Autowired
    TelegramBotFile telegramBotFile;

    public SendMessage createAnswer(Update update){

        if(update.getMessage() == null) return null;

        String text = update.getMessage().getText();

        if (update.getMessage().getDocument() != null){
            botState = BotState.ASK_SEND_FILE;
        }
        else if (text != null) {
            switch (text) {
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
        }else return null;



        return createMessage(update, botState);

    }

    private SendMessage createMessage(Update update, BotState botState){
        SendMessage sendMessage = new SendMessage();
        long chat_id = update.getMessage().getChatId();
        sendMessage.setChatId(chat_id);
        String username = update.getMessage().getFrom().getUserName();

        switch (botState){
            case START:
                System.out.println(username);

                //проверка ранее подключенных пользователей
                if(!usersTelegramBot.getUserMap().containsKey(username)){

                    String usernameSheet = botGoogleSheet.findUser(username);

                    if(usernameSheet != null){
                        String message = botMessage.welcomeMessage(usernameSheet);
                        MainMenu mainMenu = new MainMenu();
                        sendMessage = mainMenu.getMainMenuMessage(chat_id, message);

                        File folderDirectory = telegramBotGoogleDrive.activate(usernameSheet);

                        usersTelegramBot.getUserMap().put(username, new User(username, usernameSheet, folderDirectory));

                        System.out.println(username + " = " + usersTelegramBot.getUserMap().get(username));

                    }else {

                        sendMessage.setText(botMessage.negativeMessage());
                        sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
                    }
                }else {
                    if(usersTelegramBot.getUserMap().get(username).isSendHomework()){
                        usersTelegramBot.getUserMap().get(username).setSendHomework(false);
                    }
                    String message = botMessage.welcomeMessage(usersTelegramBot.getUserMap().get(username).getUsernameSheet());
                    MainMenu mainMenu = new MainMenu();
                    sendMessage = mainMenu.getMainMenuMessage(chat_id, message);

                    System.out.println(username + " = " + usersTelegramBot.getUserMap().get(username));
                }

                break;
            case ASK_SUPPORT:
                if(usersTelegramBot.getUserMap().containsKey(username) && usersTelegramBot.getUserMap().get(username).isSendHomework()){
                    usersTelegramBot.getUserMap().get(username).setSendHomework(false);
                }
                Support support = new Support();
                sendMessage = support.getSupportMessage(chat_id);
                break;
            case ASK_ADMIN:
                if(usersTelegramBot.getUserMap().containsKey(username) && usersTelegramBot.getUserMap().get(username).isSendHomework()){
                    usersTelegramBot.getUserMap().get(username).setSendHomework(false);
                }
                sendMessage.setText(botMessage.messageAdmin());
                break;
            case ASK_SPIKER:
                if(usersTelegramBot.getUserMap().containsKey(username) && usersTelegramBot.getUserMap().get(username).isSendHomework()){
                    usersTelegramBot.getUserMap().get(username).setSendHomework(false);
                }
                sendMessage.setText(botMessage.messageSpiker());
                break;
            case ASK_MAIN_MENU:
                if(usersTelegramBot.getUserMap().containsKey(username) && usersTelegramBot.getUserMap().get(username).isSendHomework()){
                    usersTelegramBot.getUserMap().get(username).setSendHomework(false);
                }
                MainMenu mainMenu = new MainMenu();
                sendMessage = mainMenu.getMainMenuMessage(chat_id, "меню");
                break;
            case ASK_SEND_HOMEWORK:

                System.out.println(username + " = " + usersTelegramBot.getUserMap().get(username));

                sendMessage.setText("загрузите дз пожалуйста");
                username = update.getMessage().getFrom().getUserName();

                if(usersTelegramBot.getUserMap().containsKey(username)) {
                    usersTelegramBot.getUserMap().get(username).setSendHomework(true);
                }
                break;
            case ASK_SEND_FILE:
                username = update.getMessage().getFrom().getUserName();

                System.out.println(username + " = " + usersTelegramBot.getUserMap().get(username));


                if(usersTelegramBot.getUserMap().containsKey(username) && usersTelegramBot.getUserMap().get(username).isSendHomework()){
                    String fileId = update.getMessage().getDocument().getFileId();
                    String fileName = update.getMessage().getDocument().getFileName();
                    File userFolder = usersTelegramBot.getUserMap().get(username).getUserDirectory();
                    System.out.println("fileId = " +fileId);

                    try (InputStream inputStream = telegramBotFile.uploadUserFile(fileName, fileId)){
                        String text = telegramBotGoogleDrive.sendHomework(inputStream, fileName, userFolder);
                        sendMessage.setText(text);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    usersTelegramBot.getUserMap().get(username).setSendHomework(false);

                    System.out.println("отправлено");
                }
                break;
            case ASK_PASSWORD:
                if(usersTelegramBot.getUserMap().containsKey(username) && usersTelegramBot.getUserMap().get(username).isSendHomework()){
                    usersTelegramBot.getUserMap().get(username).setSendHomework(false);
                }
                sendMessage.setChatId(chat_id);
                sendMessage.setText("в разработке");
                break;
            case ANOTHER:
                if(usersTelegramBot.getUserMap().containsKey(username) && usersTelegramBot.getUserMap().get(username).isSendHomework()){
                    usersTelegramBot.getUserMap().get(username).setSendHomework(false);
                }
                sendMessage.setChatId(chat_id);
                sendMessage.setText("Моя, твоя, не понимать!");
                break;

        }


        return sendMessage;
    }

}
