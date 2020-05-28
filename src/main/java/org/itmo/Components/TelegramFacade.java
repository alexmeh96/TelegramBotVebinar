package org.itmo.Components;

import com.google.api.services.drive.model.File;
import org.itmo.Components.botButton.TelegramButton;
import org.itmo.Components.botFile.TelegramBotFile;
import org.itmo.Components.googleDrive.TelegramBotGoogleDrive;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.Components.model.QuestionUser;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.itmo.MainTelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//обработка сообщения
@Component
public class TelegramFacade {

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

        if(!update.hasMessage()) return null;

        SendMessage sendMessage = new SendMessage();
        long chat_id = update.getMessage().getChatId();
        sendMessage.setChatId(chat_id);
        String username = update.getMessage().getFrom().getUserName();

        String textMessage = update.getMessage().getText();

        //обработка отправленного файла пользователем
        if (update.getMessage().hasDocument()){

            System.out.println(username + " = " + telegramUsers.getUserMap().get(username));

            if(telegramUsers.getUserMap().containsKey(username) && telegramUsers.getUserMap().get(username).isSendHomework()){
                String fileId = update.getMessage().getDocument().getFileId();
                String fileName = update.getMessage().getDocument().getFileName();
                File userFolder = telegramUsers.getUserMap().get(username).getUserDirectory();


                try (InputStream inputStream = telegramBotFile.uploadUserFile(fileName, fileId)){
                    boolean res = telegramBotGoogleDrive.sendHomework(
                            inputStream,
                            fileName,
                            "дз" + telegramUsers.getUserMap().get(username).getNumFile(),
                            userFolder);
                    if(res)
                        sendMessage.setText(botMessage.cashFoHW(telegramUsers,
                                                                telegramUsers.getUserMap().get(username),
                                                                new Date(update.getMessage().getDate()*1000l)));
                    else{
                        sendMessage.setText("не удалось отправить домашнее задание!");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                telegramUsers.getUserMap().get(username).setSendHomework(false);
                telegramUsers.getUserMap().get(username).setAskQuestion(false);



                System.out.println("Домашнее задание отправлено!");
            }
        }
        else if (!textMessage.isEmpty()) {
            switch (textMessage) {
                case "/start":
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

                            telegramUsers.getUserMap().put(username, new User(chat_id, username, userData.get("nameSheet"), folderDirectory, Integer.parseInt(userData.get("cash"))));

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
                case "Тех поддержка":
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
                case "Админ":
                    if(telegramUsers.getUserMap().containsKey(username)){
                        telegramUsers.getUserMap().get(username).setSendHomework(false);
                        telegramUsers.getUserMap().get(username).setAskQuestion(false);
                    }
                    sendMessage.setText(botMessage.messageAdmin());
                    break;
                case "Задать вопрос":
                    if(telegramUsers.getUserMap().containsKey(username)){
                        telegramUsers.getUserMap().get(username).setSendHomework(false);
                        telegramUsers.getUserMap().get(username).setAskQuestion(true);
                    }
                    sendMessage.setText("Введите ваш вопрос");
                    break;
                case "Главное меню":
                    if(telegramUsers.getUserMap().containsKey(username)){
                        telegramUsers.getUserMap().get(username).setSendHomework(false);
                        telegramUsers.getUserMap().get(username).setAskQuestion(false);
                    }
                    stringList = new ArrayList<>();
                    stringList.add("Отправить дз");
                    stringList.add("Тех поддержка");
                    stringList.add("Топ студентов");
                    stringList.add("Пароль от личного кабинета");
                    telegramButton.setButtonList(stringList);
                    sendMessage = telegramButton.getMainMenuMessage(chat_id, "Меню");
                    break;
                case "Топ студентов":

                    break;
                case "Отправить дз":
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
//                    try {
//                    SendPhoto sendPhoto = new SendPhoto();
//                    sendPhoto.setChatId(chat_id);
//                    java.io.File file = new java.io.File("/home/alex/work/java/Projects/TelegramBotVebinar/src/main/resources/1.png");
//                    InputStream inputStream = new FileInputStream(file);
//                    sendPhoto.setCaption("erjlfjhreljjljre\nerijvioerjoimroeimre");
//                    sendPhoto.setPhoto("lp1", inputStream);
//
//
//                        mainTelegramBot.execute(sendPhoto);
//                    } catch (TelegramApiException e) {
//                        e.printStackTrace();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }


                    break;
                case "Сделать рассылку дз":
                    stringList = new ArrayList<>();
                    stringList.add("Домашнее задание 1");
                    stringList.add("Домашнее задание 2");
                    stringList.add("Домашнее задание 3");
                    stringList.add("Меню");
                    telegramButton.setButtonList(stringList);
                    sendMessage = telegramButton.getMainMenuMessage(chat_id, "сделать рассылку дз");

                    break;
                case "Домашнее задание 1":
                case "Домашнее задание 2":
                case "Домашнее задание 3":
                    if (telegramUsers.getAdminList().contains(update.getMessage().getFrom().getUserName())) {
                        String num = update.getMessage().getText().substring(17);
                        try {
                            SendVideo sendVideo = new SendVideo();
                            String fileId = telegramBotGoogleDrive.getFileVideoId(telegramBotGoogleDrive.getFileMapHW().get("HW_"+num).getId());
                            sendVideo.setCaption(telegramBotGoogleDrive.getTextHW(telegramBotGoogleDrive.getFileMapHW().get("HW_"+num).getId()));
                            sendVideo.setVideo("https://drive.google.com/uc?id=" + fileId + "&authuser=1&export=download");

                            telegramUsers.getMapDate().put(num, new Date(update.getMessage().getDate()*1000l));

                            for (User user : telegramUsers.getUserMap().values()) {
                                sendVideo.setChatId(user.getChatId());
                                mainTelegramBot.execute(sendVideo);
                            }
                        } catch (IOException | TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(telegramUsers.getUserMap().containsKey(username)){
                        username = update.getMessage().getFrom().getUserName();

                        telegramUsers.getUserMap().get(username).setSendHomework(true);

                        String num = update.getMessage().getText().substring(17);
                        telegramUsers.getUserMap().get(username).setNumFile(num);
                        sendMessage.setText("Пожалйста загрузите домашнее задание " + num);
                        System.out.println(username + " = " + telegramUsers.getUserMap().get(username));

                    }
                    break;

                case  "Меню":
                    stringList = new ArrayList<>();
                    stringList.add("Список вопросов");
                    stringList.add("Сделать рассылку");
                    stringList.add("Сделать рассылку дз");
                    telegramButton.setButtonList(stringList);
                    sendMessage = telegramButton.getMainMenuMessage(chat_id, "Меню");
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
        }else return null;

        return sendMessage;

    }


}
