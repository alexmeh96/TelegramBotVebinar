package org.itmo.Components;

import com.google.api.services.drive.model.File;
import lombok.extern.slf4j.Slf4j;
import org.itmo.Components.botButton.TelegramButton;
import org.itmo.Components.botFile.TelegramBotFile;
import org.itmo.Components.googleDrive.TelegramBotGoogleDrive;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.Components.model.Admin;
import org.itmo.Components.model.QuestionUser;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.itmo.MainTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.util.*;

//обработка сообщения
@SuppressWarnings("ALL")
@Slf4j
@Component
public class TelegramFacade {
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(TelegramFacade.class);

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

    private BotApiMethod<?> eventHasText(Update update){
        SendMessage sendMessage = new SendMessage();
        long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId);
        String username = update.getMessage().getFrom().getUserName();

        String textMessage = update.getMessage().getText();
        switch (textMessage) {
            case "/start":

                log.info("{} стартует", username);

                if(botGoogleSheet.findAdminTable(username)){
                    telegramUsers.getAdminMap().put(username, new Admin(username));

                    List<String> stringList = new ArrayList<>();
                    stringList.add("Список вопросов");
                    stringList.add("Сделать рассылку");
                    stringList.add("Сделать рассылку дз");
                    telegramButton.setButtonListText(stringList);
                    sendMessage = telegramButton.getMainMenuMessage(chatId, "Здравствуйте администратор!");
                    break;
                }

                //проверка ранее подключенных пользователей
                if(!telegramUsers.getUserMap().containsKey(username)){


                    Map<String, String> userData = botGoogleSheet.findUser(username);
                    if(!userData.isEmpty()){
                        String message = botMessage.welcomeMessage(userData.get("nameSheet"));

                        List<String> stringList = new ArrayList<>();
                        stringList.add("Отправить домашнее задание");
                        stringList.add("Связаться со службой поддержки");
                        stringList.add("Пароль от личного кабинета");
                        telegramButton.setButtonListText(stringList);
                        sendMessage = telegramButton.getMainMenuMessage(chatId, message);

                        File folderDirectory = telegramBotGoogleDrive.activate(userData.get("nameSheet"));

                        telegramUsers.getUserMap().put(username, new User(chatId, username, userData.get("nameSheet"), folderDirectory, Integer.parseInt(userData.get("cash"))));

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
                    stringList.add("Отправить домашнее задание");
                    stringList.add("Связаться со службой поддержки");
                    stringList.add("Пароль от личного кабинета");
                    telegramButton.setButtonListText(stringList);
                    sendMessage = telegramButton.getMainMenuMessage(chatId, message);

                    log.info("{} = {}", username, telegramUsers.getUserMap().get(username));

                }
                break;
            case "Связаться со службой поддержки":
                if(telegramUsers.getUserMap().containsKey(username) ){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                }
                List<String> stringList = new ArrayList<>();
                stringList.add("Связаться с администратором");
                stringList.add("Задать вопрос ведущему");
                stringList.add("Назад");
                telegramButton.setButtonListText(stringList);
                sendMessage = telegramButton.getMainMenuMessage(chatId, "Тех поддержка");

                break;
            case "Связаться с администратором":
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                }
                sendMessage.setText(botMessage.messageAdmin());
                break;
            case "Задать вопрос ведущему":
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(true);
                }
                sendMessage.setText("Введите ваш вопрос");
                break;
            case "Назад":
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                }
                stringList = new ArrayList<>();
                stringList.add("Отправить домашнее задание");
                stringList.add("Связаться со службой поддержки");
                stringList.add("Рейтинг участников");
                stringList.add("Пароль от личного кабинета");
                telegramButton.setButtonListText(stringList);
                sendMessage = telegramButton.getMainMenuMessage(chatId, "Меню");
                break;
            case "Рейтинг участников":

                break;
            case "Отправить домашнее задание":
            case "Сделать рассылку дз":
                stringList = new ArrayList<>();
                stringList.add("дз1");
                stringList.add("дз2");
                stringList.add("дз3");
                List<String> stringId = new ArrayList<>();
                stringId.add("hw1");
                stringId.add("hw2");
                stringId.add("hw3");

                telegramButton.setButtonListText(stringList);
                telegramButton.setButtonListId(stringId);
                sendMessage.setReplyMarkup(telegramButton.createInlineButton());
                sendMessage.setText("Выберите домашнее задание:");

                break;
            case "Пароль от личного кабинета":
                if(telegramUsers.getUserMap().containsKey(username)){
                    telegramUsers.getUserMap().get(username).setSendHomework(false);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                    String password = botGoogleSheet.returnPass(username);
                    sendMessage.setText(password);
                }else
                    sendMessage.setText("Извините, но я вас не понимаю");
                break;
            case "Список вопросов":
                sendMessage.setText("<b>Список вопросов за последние 48 часов:</b>\n" + botMessage.questionList(telegramUsers, new Date(update.getMessage().getDate()* 1000l)));
                sendMessage.setParseMode("HTML");
                break;
            case "Сделать рассылку":
                stringList = new ArrayList<>();
                stringList.add("текст");
                stringList.add("текст и видео");
                stringList.add("текст и картинка");
                stringId = new ArrayList<>();
                stringId.add("text");
                stringId.add("textVideo");
                stringId.add("textImage");

                telegramButton.setButtonListText(stringList);
                telegramButton.setButtonListId(stringId);
                sendMessage.setReplyMarkup(telegramButton.createInlineButton());
                sendMessage.setText("Выберите тип рассылки:");

                break;
            default:
                if(telegramUsers.getUserMap().containsKey(username) && telegramUsers.getUserMap().get(username).isAskQuestion()){
                    Date date = new Date(update.getMessage().getDate()* 1000l);

                    String text = update.getMessage().getText();
                    telegramUsers.getUserMap().get(username).getListQuestion().add(new QuestionUser(text, date));
                    System.out.println(text);
                    telegramUsers.getUserMap().get(username).setAskQuestion(false);
                    sendMessage.setText("Ваш вопрос отправлен!");
                    break;
                }
                sendMessage.setText("Извините, но я вас не понимаю!");
                break;
        }
        return sendMessage;

    }

    private BotApiMethod<?> eventHasDocument(Update update){

        String username = update.getMessage().getFrom().getUserName();

        log.info("{} = {}", username, telegramUsers.getUserMap().get(username));

        if(telegramUsers.getUserMap().containsKey(username) && telegramUsers.getUserMap().get(username).isSendHomework()){
            SendMessage sendMessage = new SendMessage();
            long chat_id = update.getMessage().getChatId();
            sendMessage.setChatId(chat_id);

            String fileId = update.getMessage().getDocument().getFileId();
            String fileName = update.getMessage().getDocument().getFileName();
            File userFolder = telegramUsers.getUserMap().get(username).getUserDirectory();

            try (InputStream inputStream = telegramBotFile.uploadUserFile(fileId)){
                boolean res = telegramBotGoogleDrive.sendHomework(
                        inputStream,
                        fileName,
                        "Домашнее задание " + telegramUsers.getUserMap().get(username).getNumFile(),
                        userFolder);
                if(res)
                    sendMessage.setText(botMessage.cashFoHW(telegramUsers,
                            telegramUsers.getUserMap().get(username),
                            new Date(update.getMessage().getDate()*1000l)));
                else{
                    sendMessage.setText("Не удалось отправить домашнее задание!");
                }
            }catch (Exception e){
                log.warn("Не удалось отправить домашнее задание! \n {}", e.getStackTrace());
            }

            telegramUsers.getUserMap().get(username).setSendHomework(false);
            telegramUsers.getUserMap().get(username).setAskQuestion(false);

            log.info("Домашнее задание отправлено!");
            return sendMessage;

        }else if (telegramUsers.getAdminMap().containsKey(username)) {
            Admin admin = telegramUsers.getAdminMap().get(username);

            if (admin.isUploadText()) {

                SendMessage sendMessage = new SendMessage();
                long chatId = update.getMessage().getChatId();

                String text = null;
                try {
                    text = telegramBotFile.getTextFile(update.getMessage().getDocument().getFileId());
                } catch (IOException e) {
                    e.printStackTrace();
                    admin.uploadFalse();
                    return sendMessage.setChatId(chatId).setText("не удалось загрузить файл с текстом!");
                }
                if (admin.isUploadVideo()) {
                    admin.setText(text);
                    return sendMessage.setChatId(chatId).setText("загрузите видео");
                } else if (admin.isUploadPhoto()) {
                    admin.setText(text);
                    return sendMessage.setChatId(chatId).setText("загрузите картинку");
                } else {
                    sendMessage.setText(text);
                    try {
                        for (User user : telegramUsers.getUserMap().values()) {
                            sendMessage.setChatId(user.getChatId());
                            mainTelegramBot.execute(sendMessage);
                        }
                        sendMessage.setChatId(chatId);
                        mainTelegramBot.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        admin.uploadFalse();
                        return sendMessage.setChatId(chatId).setText("не удалось разослать текст!");

                    }
                    admin.uploadFalse();
                    return sendMessage.setChatId(chatId).setText("рассылка текста прошла успешно!");
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> eventHasVideo(Update update){
        String username = update.getMessage().getFrom().getUserName();
        if (telegramUsers.getAdminMap().containsKey(username)) {
            Admin admin = telegramUsers.getAdminMap().get(username);
            if (admin.isUploadVideo()) {
                SendMessage sendMessage = new SendMessage();
                long chatId = update.getMessage().getChatId();

                String fileId = update.getMessage().getVideo().getFileId();
                try (InputStream inputStream = telegramBotFile.uploadUserFile(fileId)) {
                    SendVideo sendVideo = new SendVideo();
                    sendVideo.setVideo("video", inputStream);
                    sendVideo.setCaption(admin.getText());
                    try {
                        for (User user : telegramUsers.getUserMap().values()){
                            sendVideo.setChatId(user.getChatId());
                            mainTelegramBot.execute(sendVideo);
                        }
                        sendVideo.setChatId(chatId);
                        mainTelegramBot.execute(sendVideo);
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                        admin.uploadFalse();
                        return sendMessage.setChatId(chatId).setText("не удалось разослать видео с текстом!");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    admin.uploadFalse();
                    return sendMessage.setChatId(chatId).setText("не удалось загрузить видео!");
                }
                admin.uploadFalse();
                return sendMessage.setChatId(chatId).setText("текст с видео разосланы успешно!");
            }

        }
        return null;
    }

    private BotApiMethod<?> eventHasPhoto(Update update){
        String username = update.getMessage().getFrom().getUserName();
        if (telegramUsers.getAdminMap().containsKey(username)){
            Admin admin = telegramUsers.getAdminMap().get(username);
            if (admin.isUploadPhoto()) {
                SendMessage sendMessage = new SendMessage();
                long chatId = update.getMessage().getChatId();

                String fileId = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1).getFileId();
                try (InputStream inputStream = telegramBotFile.uploadUserFile(fileId)) {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setPhoto("photo", inputStream);
                    sendPhoto.setCaption(admin.getText());

                    try {
                        for (User user : telegramUsers.getUserMap().values()){
                            sendPhoto.setChatId(user.getChatId());
                            mainTelegramBot.execute(sendPhoto);
                        }
                        sendPhoto.setChatId(chatId);
                        mainTelegramBot.execute(sendPhoto);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        admin.uploadFalse();
                        return sendMessage.setChatId(chatId).setText("не удалось разослать картинку с текстом!");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    admin.uploadFalse();
                    return sendMessage.setChatId(chatId).setText("не удалось загрузить картинку!");
                }
                admin.uploadFalse();
                return sendMessage.setChatId(chatId).setText("текст с фото разосланы успешно!");
            }
        }
        return null;
    }

    private BotApiMethod<?> eventInlineButton(Update update){
        String username = update.getCallbackQuery().getFrom().getUserName();

        if (telegramUsers.getAdminMap().containsKey(username)) {
            String answer = "";
            String buttonId = update.getCallbackQuery().getData();

            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            EditMessageText editMessageText = new EditMessageText()
                    .setChatId(chatId)
                    .setMessageId(messageId);

            if (buttonId.startsWith("hw")) {
                String num = update.getCallbackQuery().getData().substring(2);

                SendVideo sendVideo = new SendVideo();
                String fileId = telegramBotGoogleDrive.getFileVideoId(telegramBotGoogleDrive.getFileMapHW().get("HW_" + num).getId());
                sendVideo.setCaption(telegramBotGoogleDrive.getTextHW(telegramBotGoogleDrive.getFileMapHW().get("HW_" + num).getId()));
                sendVideo.setVideo("https://drive.google.com/uc?id=" + fileId + "&authuser=1&export=download");

               // try(InputStream inputStream = new URL("https://drive.google.com/uc?id=" + fileId + "&authuser=1&export=download").openStream();) {
                    //sendVideo.setVideo("video", inputStream);
                    try {
                        for (User user : telegramUsers.getUserMap().values()) {
                            sendVideo.setChatId(user.getChatId());
                            mainTelegramBot.execute(sendVideo);
                        }
                        sendVideo.setChatId(chatId);
                        mainTelegramBot.execute(sendVideo);

                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                        return editMessageText.setText("Не удалось выполнить рассылку дз " + num);

                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return editMessageText.setText("Не удалось получить файлы из GoogleDriver!" + num);
//
//                }
                telegramUsers.getMapDate().put(num, new Date(update.getCallbackQuery().getMessage().getDate() * 1000l));

                return editMessageText.setText("Рассылка дз " + num + " прошла успешно!");

            }
            switch (buttonId) {
                case "text":
                    telegramUsers.getAdminMap().get(username).setUploadText(true);
                    telegramUsers.getAdminMap().get(username).setUploadVideo(false);
                    telegramUsers.getAdminMap().get(username).setUploadPhoto(false);
                    return editMessageText.setText("Загрузите файл с текстом");
                case "textVideo":
                    telegramUsers.getAdminMap().get(username).setUploadText(true);
                    telegramUsers.getAdminMap().get(username).setUploadVideo(true);
                    telegramUsers.getAdminMap().get(username).setUploadPhoto(false);
                    return editMessageText.setText("Загрузите файл с текстом");
                case "textImage":
                    telegramUsers.getAdminMap().get(username).setUploadText(true);
                    telegramUsers.getAdminMap().get(username).setUploadPhoto(true);
                    telegramUsers.getAdminMap().get(username).setUploadVideo(false);
                    return editMessageText.setText("Загрузите файл с текстом");
            }
            return null;
        }
        if(telegramUsers.getUserMap().containsKey(username)) {

            telegramUsers.getUserMap().get(username).setSendHomework(true);

            String num = update.getCallbackQuery().getData().substring(2);
            telegramUsers.getUserMap().get(username).setNumFile(num);
            System.out.println(num);
            Integer message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            String answer = "Пожалуйста загрузите домашнее задание " + num;
            EditMessageText new_message = new EditMessageText()
                    .setChatId(chat_id)
                    .setMessageId(message_id)
                    .setText(answer);

            log.info("{} = {}", username, telegramUsers.getUserMap().get(username));

            return new_message;

        }
        return null;
    }

    public BotApiMethod<?> createAnswer(Update update){

        if (update.hasCallbackQuery()){
            return eventInlineButton(update);
        }
        if (update.getMessage().hasDocument()) {
            return eventHasDocument(update);
        }
        if (update.getMessage().hasPhoto()){
            return eventHasPhoto(update);
        }

        if (update.getMessage().hasVideo()) {
            return eventHasVideo(update);
        }
        if (update.getMessage().hasText()) {
            return eventHasText(update);
        }

        return null;
    }


}
