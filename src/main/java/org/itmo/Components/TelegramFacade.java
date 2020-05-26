package org.itmo.Components;

import com.google.api.services.drive.model.File;
import org.itmo.Components.botButton.TelegramButton;
import org.itmo.Components.botFile.TelegramBotFile;
import org.itmo.Components.googleDrive.TelegramBotGoogleDrive;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.itmo.MainTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//обработка сообщения
@Component
public class TelegramFacade {

    private BotState botState;

    @Autowired
    MainTelegramBot mainTelegramBot;
    @Autowired
    BotGoogleSheet botGoogleSheet;
    @Autowired
    BotMessage botMessage;
    @Autowired
    TelegramBotGoogleDrive telegramBotGoogleDrive;
    @Autowired
    TelegramUsers telegramUsers;
    @Autowired
    TelegramBotFile telegramBotFile;
    @Autowired
    TelegramButton telegramButton;

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
                case "Тех поддержка":
                    botState = BotState.ASK_SUPPORT;
                    break;
                case "Админ":
                    botState = BotState.ASK_ADMIN;
                    break;
                case "Задать вопрос":
                    botState = BotState.ASK_QUESTION;
                    break;
                case "Спикер":
                    botState = BotState.ASK_SPIKER;
                    break;
                case "Главное меню":
                    botState = BotState.ASK_MAIN_MENU;
                    break;
                case "Отправить дз":
                    botState = BotState.ASK_SEND_HOMEWORK;
                    break;
                case "Пароль от личного кабинета":
                    botState = BotState.ASK_PASSWORD;
                    break;
                case "Список вопросов":
                    botState = BotState.ADMIN_LIST_QUESTION;
                    break;
                case "Сделать рассылку":
                    botState = BotState.ADMIN_MAKE_SEND;
                    break;
                case "Сделать рассылку дз":
                    botState = BotState.ADMIN_MAKE_SEND_HW;
                    break;
                case "Домашнее задание 1":
                case "Домашнее задание 2":
                case "Домашнее задание 3":
                    if (telegramUsers.getAdminList().contains(update.getMessage().getFrom().getUserName()))
                        botState = BotState.ADMIN_HW1;
                    else botState = BotState.USER_HW;
                    break;

                case  "Меню":
                    botState = BotState.ADMIN_MENU;
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

                if(botGoogleSheet.findAdminTable(username)){
                    telegramUsers.getAdminList().add(username);

                    List<String> stringList = new ArrayList<>();
                    stringList.add("Список вопросов");
                    stringList.add("Сделать рассылку");
                    stringList.add("Сделать рассылку дз");
                    telegramButton.setButtonList(stringList);
                    sendMessage = telegramButton.getMainMenuMessage(chat_id, "Здравствуйте администратор!");
                    break;
                }

                //проверка ранее подключенных пользователей
                if(!telegramUsers.getUserMap().containsKey(username)){


                    Map<String, String> userData = botGoogleSheet.findUser(username);
                    if(userData != null){
                        String message = botMessage.welcomeMessage(userData.get("nameSheet"));

                        List<String> stringList = new ArrayList<>();
                        stringList.add("Отправить дз");
                        stringList.add("Тех поддержка");
                        stringList.add("Пароль от личного кабинета");
                        telegramButton.setButtonList(stringList);
                        sendMessage = telegramButton.getMainMenuMessage(chat_id, message);

                        File folderDirectory = telegramBotGoogleDrive.activate(userData.get("nameSheet"));

                        telegramUsers.getUserMap().put(username, new User(chat_id, username, userData.get("nameSheet"), folderDirectory));

                        System.out.println(username + " = " + telegramUsers.getUserMap().get(username));

                    }else {

                        sendMessage.setText(botMessage.negativeMessage());
                        sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
                    }
                }else {

                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);

                    String message = botMessage.welcomeMessage(telegramUsers.getUserMap().get(username).getUsernameSheet());

                    List<String> stringList = new ArrayList<>();
                    stringList.add("Отправить дз");
                    stringList.add("Тех поддержка");
                    stringList.add("Пароль от личного кабинета");
                    telegramButton.setButtonList(stringList);
                    sendMessage = telegramButton.getMainMenuMessage(chat_id, message);

                    System.out.println(username + " = " + telegramUsers.getUserMap().get(username));
                }

                break;
            case ASK_SUPPORT:
                if(telegramUsers.getUserMap().containsKey(username) ){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                }
                List<String> stringList = new ArrayList<>();
                stringList.add("Админ");
                stringList.add("Задать вопрос");
                stringList.add("Главное меню");
                telegramButton.setButtonList(stringList);
                sendMessage = telegramButton.getMainMenuMessage(chat_id, "Поддержка");
                break;
            case ASK_ADMIN:
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                }
                sendMessage.setText(botMessage.messageAdmin());
                break;
            case ASK_QUESTION:
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(true);
                }
                sendMessage.setText("Введите ваш вопрос");
                break;
            case ASK_MAIN_MENU:
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                }
                stringList = new ArrayList<>();
                stringList.add("Отправить дз");
                stringList.add("Тех поддержка");
                stringList.add("Пароль от личного кабинета");
                telegramButton.setButtonList(stringList);
                sendMessage = telegramButton.getMainMenuMessage(chat_id, "Меню");
                break;
            case ASK_SEND_HOMEWORK:
                stringList = new ArrayList<>();
                stringList.add("Домашнее задание 1");
                stringList.add("Домашнее задание 2");
                stringList.add("Домашнее задание 3");
                stringList.add("Главное меню");
                telegramButton.setButtonList(stringList);
                sendMessage = telegramButton.getMainMenuMessage(chat_id, "отправить домашнее задание");
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                }

                break;
            case ASK_SEND_FILE:
                username = update.getMessage().getFrom().getUserName();

                System.out.println(username + " = " + telegramUsers.getUserMap().get(username));


                if(telegramUsers.getUserMap().containsKey(username) && telegramUsers.getUserMap().get(username).isSendHomework()){
                    String fileId = update.getMessage().getDocument().getFileId();
                    String fileName = update.getMessage().getDocument().getFileName();
                    File userFolder = telegramUsers.getUserMap().get(username).getUserDirectory();


                    try (InputStream inputStream = telegramBotFile.uploadUserFile(fileName, fileId)){
                        String text = telegramBotGoogleDrive.sendHomework(
                                                                    inputStream,
                                                                    fileName,
                                                        "дз" + telegramUsers.getUserMap().get(username).getNumFile(),
                                                                     userFolder);
                        sendMessage.setText(text);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);

                    System.out.println("Домашнее задание отправлено!");
                }
                break;
            case ASK_PASSWORD:
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                    String password = botGoogleSheet.returnPass(username);
                    sendMessage.setText(password);
                }else
                    sendMessage.setText("Извините, но я вас не понимаю");

                break;
            case ADMIN_MENU:
                stringList = new ArrayList<>();
                stringList.add("Список вопросов");
                stringList.add("Сделать рассылку");
                stringList.add("Сделать рассылку дз");
                telegramButton.setButtonList(stringList);
                sendMessage = telegramButton.getMainMenuMessage(chat_id, "Меню");
                break;
            case ADMIN_LIST_QUESTION:

                break;
            case ADMIN_MAKE_SEND:
    //            try(InputStream inputStream = telegramBotGoogleDrive.downloadFile("")) {
//                    SendDocument sendDocument = new SendDocument();
//
//                    for (User user : usersTelegramBot.getUserMap().values()) {
//                        sendDocument.setChatId(user.getChatId());
//                        sendDocument.setDocument("дз1", inputStream);
//                        mainTelegramBot.execute(sendDocument);
//                    }
//                } catch (IOException | TelegramApiException e) {
//                    e.printStackTrace();
//                }
                break;
            case ADMIN_MAKE_SEND_HW:
                stringList = new ArrayList<>();
                stringList.add("Домашнее задание 1");
                stringList.add("Домашнее задание 2");
                stringList.add("Домашнее задание 3");
                stringList.add("Меню");
                telegramButton.setButtonList(stringList);
                sendMessage = telegramButton.getMainMenuMessage(chat_id, "сделать рассылку дз");
                break;

            case USER_HW:

                username = update.getMessage().getFrom().getUserName();


                telegramUsers.getUserMap().get(username).setSendHomework(true);

                String num = update.getMessage().getText().substring(17);
                telegramUsers.getUserMap().get(username).setNumFile(num);
                sendMessage.setText("Пожалйста загрузите домашнее задание " + num);
                System.out.println(username + " = " + telegramUsers.getUserMap().get(username));

                break;

            case ADMIN_HW1:
                try {
                    SendVideo sendVideo = new SendVideo();
                    sendVideo.setChatId(chat_id);
                    String fileId = telegramBotGoogleDrive.getFileId(telegramBotGoogleDrive.getFileMapHW().get("HW_1").getId());
                    System.out.println("https://drive.google.com/uc?id=" + fileId + "&authuser=1&export=download");
                 //   sendVideo.setVideo("https://drive.google.com/uc?id=" + fileId + "&authuser=1&export=download");
                //    mainTelegramBot.execute(sendVideo);
                } catch ( IOException e) {
                    e.printStackTrace();
                }

                break;
            case ANOTHER:

                if(telegramUsers.getUserMap().containsKey(username) && telegramUsers.getUserMap().get(username).isAskQuestion()){
                    Date date = new Date(update.getMessage().getDate()* 1000l);
                    String text = update.getMessage().getText() + "\n" + new SimpleDateFormat("dd.MM HH:mm").format(date);
                    telegramUsers.getUserMap().get(username).getListQuestion().add(text);
                    System.out.println(text);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                    sendMessage.setText("Ваш вопрос отправлен!");
                    break;
                }


                sendMessage.setText("Извините, но я вас не понимаю");
                break;
        }

        return sendMessage;
    }

}
