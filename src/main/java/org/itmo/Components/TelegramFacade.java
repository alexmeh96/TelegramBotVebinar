package org.itmo.Components;

import com.google.api.services.drive.model.File;
import lombok.extern.slf4j.Slf4j;
import org.itmo.Components.service.TelegramButton;
import org.itmo.Components.service.TelegramBotFile;
import org.itmo.Components.googleDrive.TelegramBotGoogleDrive;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.Components.model.Admin;
import org.itmo.Components.model.Question;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.itmo.Components.service.BotMessage;
import org.itmo.MainTelegramBot;
import org.itmo.config.BotProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Класс обработки сообщений от чатов и создание ответов им
 */
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

    /**
     * обработка текстовых сообщений и статических кнопок
     * @param update
     * @return ответ на пришедший update
     */
    private BotApiMethod<?> eventHasText(Update update){
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        String username = update.getMessage().getFrom().getUserName();

        switch (update.getMessage().getText()) {
            case "/start":
                log.info("{} стартует", username);
                if(telegramUsers.getAdminMap().isEmpty()){        //если map админа пуст
                    Map<String, String> userData = botGoogleSheet.findUser(username);    //получаем map данных из главной таблицы по имени пользователя

                    if(!userData.isEmpty()) {  //если map данных не пуст
                        telegramUsers.getAdminMap().put(username, new Admin(username, chatId));   //добавляем в map админов нового админа
                        try {
                            BotGoogleSheet.Update(BotProperty.SHEET_ROLE_COL, "2", "1");   //ставим 1 в ячейку role новой таблицы
                        } catch (IOException | GeneralSecurityException e) {
                            e.printStackTrace();
                        }
                        sendMessage = TelegramButton.adminMenu(); //получаем админское меню
                    }else{   //если map данных пуст
                        sendMessage.setText(botMessage.negativeMessage());   //устанавливаем негативное сообщение
                        sendMessage.setReplyMarkup(new ReplyKeyboardRemove());  //удаляем статическое меню
                    }
                    sendMessage.setChatId(chatId);  //устанавливаем id чата которому будет отправленн message
                    break;
                }
                if(telegramUsers.getAdminMap().containsKey(username)){    //если пользователь есть в мэпе админов
                    telegramUsers.getAdminMap().get(username).setChatId(chatId);   //устанавливаем chatId у админа с именем username
                    telegramUsers.getAdminMap().get(username).statusFalse();  //сбрасываем состояние админа
                    sendMessage = TelegramButton.adminMenu();   //получаем админское меню
                    sendMessage.setChatId(chatId);
                    break;
                }

                if(!telegramUsers.getUserMap().containsKey(username)){    //если пользователя нет в мэпе студентов
                    Map<String, String> userData = botGoogleSheet.findUser(username);  //получаем map данных из главной таблицы по имени пользователя
                    if(!userData.isEmpty()){   //если map данных не пуст
                        SendPhoto sendPhoto = TelegramButton.userMenuPhoto();   //получаем меню студента
                        java.io.File file = new java.io.File("src/main/resources/img/start.png");
                        sendPhoto.setPhoto(file);
                        String message = botMessage.welcomeMessage(userData.get("nameSheet"));  //получаем приветствкнное сообщение студента
                        sendPhoto.setCaption(message).setChatId(chatId);

                        File folderDirectory = telegramBotGoogleDrive.activate(userData.get("nameSheet"));  //получаем директорию студента

                        User user = new User(chatId, username, userData.get("nameSheet"), folderDirectory, userData.get("row"));  //создаём студента
                        telegramUsers.getUserMap().put(username, user);  //добавляем студента в мэп студентов
                        try {
                            mainTelegramBot.execute(sendPhoto);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        return null;

                    }else {   //если map данных пуст
                        sendMessage.setText(botMessage.negativeMessage());  //негативное сообщение
                        sendMessage.setReplyMarkup(new ReplyKeyboardRemove());  //убираем статическую клаву
                    }
                }else {  //если пользователя есть в мэпе студентов
                    User user = telegramUsers.getUserMap().get(username);
                    user.setChatId(chatId);
                    user.statusFalse();  //сброс состояния студента

                    String message = BotMessage.welcomeMessage(user.getUsernameSheet());

                    sendMessage = TelegramButton.userMenu(message);
                    sendMessage.setChatId(chatId);

                    log.info("{} = {}", username, user);

                }
                break;
            case "Баланс💳":
                User user = telegramUsers.getUserMap().get(username);
                String message = user.getUsernameSheet() + ", сейчас у вас " + user.getCash() + " монет💰";
                SendPhoto sendPhoto = new SendPhoto();   //получаем меню студента
                java.io.File file = new java.io.File("src/main/resources/img/balans.png");
                sendPhoto.setPhoto(file);
                sendPhoto.setCaption(message).setChatId(chatId);
                try {
                    mainTelegramBot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;

            case "Обновить студентов":  //ADMIN
                telegramUsers.getAdminMap().get(username).statusFalse();   //сбрасываем состояние админа
                try {
                    telegramUsers.update(mainTelegramBot, chatId);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "Помощь🆘":    //STUDENT
                telegramUsers.getUserMap().get(username).statusFalse();  //сбрасываем состояние студента
                sendPhoto = TelegramButton.helpMenuPhoto();   //получаем меню студента
                file = new java.io.File("src/main/resources/img/teh.png");
                sendPhoto.setPhoto(file);
                message = "В зависимости от вашей проблемы, выберите чья помощь вам нужна🤔\n" +
                        "Администратора или организаторов курса?";
                sendPhoto.setCaption(message).setChatId(chatId);
                try {
                    mainTelegramBot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "Связаться с администратором📝":    //STUDENT
                telegramUsers.getUserMap().get(username).statusFalse();   //сбрасываем состояние студента
                sendMessage.setChatId(chatId).setText(botMessage.messageAdmin());
                break;
            case "Задать вопрос❓":   //STUDENT
                telegramUsers.getUserMap().get(username).setSendQuestion(true);
                telegramUsers.getUserMap().get(username).setSendHomework(false);
                sendMessage.setChatId(chatId).setText("Введите ваш вопрос💬");
                break;
            case "Назад⬅️":   //STUDENT
                telegramUsers.getUserMap().get(username).statusFalse();
                sendMessage = TelegramButton.userMenu("Меню");
                sendMessage.setChatId(chatId);
                break;
            case "Рейтинг студентов🥇":   //STUDENT
                telegramUsers.getUserMap().get(username).statusFalse();
                sendMessage.setChatId(chatId).setText(botMessage.topUsers(telegramUsers));
                break;
            case "Отправить домашнее задание📚":  //STUDENT
                telegramUsers.getUserMap().get(username).statusFalse();
                sendMessage = TelegramButton.sendingMainHW();
                sendMessage.setChatId(chatId);
                break;
            case "Сделать рассылку дз":   //ADMIN
                telegramUsers.getAdminMap().get(username).statusFalse();
                sendMessage = TelegramButton.sendingHW();
                sendMessage.setChatId(chatId);
                break;
            case "Пароль от личного кабинета🤫":  //STUDENT
                telegramUsers.getUserMap().get(username).statusFalse();
                String password = botGoogleSheet.returnPass(username);
                sendMessage.setChatId(chatId).setText(password);
                break;
            case "Список вопросов":  //ADMIN
                telegramUsers.getAdminMap().get(username).statusFalse();
                Date date = new Date(update.getMessage().getDate()* 1000l);    //получаем дату этого запроса
                sendMessage.setText("<b>Список вопросов за последние 48 часов:</b>\n" + botMessage.questionList(telegramUsers, date));
                sendMessage.setParseMode("HTML");    //говорим что текст сообщения будет в формате html
                sendMessage.setChatId(chatId);
                break;
            case "Сделать рассылку":  //ADMIN
                telegramUsers.getAdminMap().get(username).statusFalse();
                sendMessage = TelegramButton.sendingChoose();
                sendMessage.setChatId(chatId);
                break;
            case "/help":
                System.out.println("help");
                sendMessage.setText("<b>Функционал бота: </b>👾\n" +
                        "🟢Для отправки основного домашнего задания нажмите на кнопку <i>\"Отправить домашнее задание\"</i> и выберите нужное\n" +
                        "🟢Для отправки дополнительного домашнего задания выберите файл и напишите в текстовом сообщение <i>#отчет№ 👉🏻 (#отчет)</i>\n" +
                        "🟢Для того чтобы получить пароль нажмите на кнопку <i>\"Пароль от личного кабинета\"</i>\n" +
                        "🟢Чтобы посмотеть рейтинг студентов по количеству монет нажмите на кнопку <i>\"Рейтинг студентов\"</i>\n" +
                        "🟢Проверить свой баланс вы можете нажав на кнопку <i>\"Баланс\"</i>\n" +
                        "🟢Если у вас есть вопросы, вы можете нажать на кнопку <i>\"Помощь\"</i>").setParseMode("HTML").setChatId(chatId);
                break;
            default:
                //Если это студент и он должен отправить вопрос
                if(telegramUsers.getUserMap().containsKey(username) && telegramUsers.getUserMap().get(username).isSendQuestion()){
                    user = telegramUsers.getUserMap().get(username);
                    user.statusFalse();
                    date = new Date(update.getMessage().getDate()* 1000l);     //получаем дату отправки
                    String text = update.getMessage().getText();
                    user.getListQuestion().add(new Question(text, date));  //добавляеем студенту вопрос

                    sendMessage.setChatId(chatId).setText("Ваш вопрос отправлен!🤓");
                    break;
                }

                if(telegramUsers.getAdminMap().containsKey(username) && telegramUsers.getAdminMap().get(username).isSendOtherHW()){
                    String num = update.getMessage().getText();
                    telegramUsers.getAdminMap().get(username).setOtherHW(num);
                    sendMessage.setChatId(chatId).setText("Загрузите файл с текстом📎");
                    break;
                }

                sendMessage.setChatId(chatId).setText("Извините, но я вас не понимаю🙀\n Нажмите /help и прочитайте, что я умею😸");
                break;
        }
        return sendMessage;

    }

    /**
     * обработка документов
     * @param update
     * @return ответ на пришедший update
     * @throws TelegramApiException
     */
    private BotApiMethod<?> eventHasDocument(Update update) throws TelegramApiException {

        String username = update.getMessage().getFrom().getUserName();

        log.info("{} = {}", username, telegramUsers.getUserMap().get(username));

        //Если это студент
        if (telegramUsers.getUserMap().containsKey(username)) {
            User user = telegramUsers.getUserMap().get(username);

                String caption = update.getMessage().getCaption();
                System.out.println(caption);
                if (caption!=null && caption.toLowerCase().indexOf("#отчет") == 0) {
                    String num = caption.substring(6);

                    long chatId = update.getMessage().getChatId();
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);

                    user.statusFalse();

                    mainTelegramBot.execute(sendMessage.setText("Дополнительное домашнее задание " + caption + " отправляется!🤞🏻"));

                    String fileId = update.getMessage().getDocument().getFileId();   //id файда отправленного студентом
                    String fileName = update.getMessage().getDocument().getFileName();   //имя файла отправленного студентом
                    File userFolder = user.getUserDirectory();  //получаем студенческую папку

                    boolean sendHW = false;  //флаг проверки успешной отправки файла
                    try (InputStream inputStream = telegramBotFile.getStreamFile(fileId)) {
                        sendHW = telegramBotGoogleDrive.sendHomework(  //возвращает true если файл был успешно отправлен
                                inputStream,
                                fileName,
                                "#отчет" + num,
                                userFolder);


                    } catch (Exception e) {
                        e.getStackTrace();
                        return sendMessage.setText("Не удалось отправить дополнительное домашнее задание!⛔️\n" +
                                "Попробуйте еще раз или сразу рискните написать моему хозяину @MarkStav😉");
                    }

                    if (sendHW) {  //если дз было успешно отправлено студентом
                        Date date = new Date(update.getMessage().getDate() * 1000l);    //дата отправки дз студентом
                        String text = botMessage.cashOtherHW(telegramUsers, user, date, num);    //получение сообщения успешной отправки дз и изменение монет в таблице
                        return sendMessage.setText(text);
                    }

                    return sendMessage.setText("Не удалось отправить дополнительное домашнее задание!⛔️\n" +
                            "Попробуйте еще раз или сразу рискните написать моему хозяину @MarkStav😉");
                }
            //если студент должен отправить дз
            if (user.isSendHomework()) {
                long chatId = update.getMessage().getChatId();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);

                user.statusFalse();

                mainTelegramBot.execute(sendMessage.setText("Домашнее задание " + user.getNumFile() + " отправляется!💌"));

                String fileId = update.getMessage().getDocument().getFileId();   //id файда отправленного студентом
                String fileName = update.getMessage().getDocument().getFileName();   //имя файла отправленного студентом
                File userFolder = user.getUserDirectory();  //получаем студенческую папку

                boolean sendHW = false;  //флаг проверки успешной отправки файла
                try (InputStream inputStream = telegramBotFile.getStreamFile(fileId)) {
                    sendHW = telegramBotGoogleDrive.sendHomework(  //возвращает true если файл был успешно отправлен
                            inputStream,
                            fileName,
                            "Домашнее задание " + user.getNumFile(),
                            userFolder);

                } catch (Exception e) {
                    e.getStackTrace();
                    return sendMessage.setText("Не удалось отправить домашнее задание!⛔️\n" +
                            "Попробуйте еще раз или сразу рискните написать моему хозяину @MarkStav😉");
                }

                if (sendHW) {  //если дз было успешно отправлено студентом
                    Date date = new Date(update.getMessage().getDate() * 1000l);    //дата отправки дз студентом
                    String text = botMessage.cashHW(telegramUsers, user, date);    //получение сообщения успешной отправки дз и изменение монет в таблице
                    if (text == null) return null;
                    return sendMessage.setText(text);
                }

                return sendMessage.setText("Не удалось отправить домашнее задание!⛔️\n" +
                        "Попробуйте еще раз или сразу рискните написать моему хозяину @MarkStav😉");
            }
        }

        //если это админ и он загружает текст
        if (telegramUsers.getAdminMap().containsKey(username) && telegramUsers.getAdminMap().get(username).isUploadText()) {
            Admin admin = telegramUsers.getAdminMap().get(username);

            SendMessage sendMessage = new SendMessage();
            long chatId = update.getMessage().getChatId();
            sendMessage.setChatId(chatId);

            String text = null;
            try {
                text = telegramBotFile.getTextFile(update.getMessage().getDocument().getFileId());  //получение текста из файла
            } catch (IOException e) {
                e.printStackTrace();
                admin.statusFalse();
                return sendMessage.setText("Не удалось загрузить файл с текстом!");
            }

            if (admin.isUploadVideo()) {  //если админ должен загрузить ещё и видео
                admin.setText(text);    //сохраняем загруженный текст в его состояние
                if (admin.getHW().isEmpty())   //если номер домашнего задания для рассылки пуст
                    return sendMessage.setText("Загрузите видео");
                else
                    return sendMessage.setText("Загрузите видео для дз " + admin.getHW());
            } else if (admin.isUploadPhoto()) { //если админ должен загрузить ещё и изображение
                admin.setText(text);   //сохраняем загруженный текст в его состояние
                return sendMessage.setText("Загрузите картинку");
            }else if (admin.isUploadFile()) { //если админ должен загрузить ещё и файл
                admin.setText(text);   //сохраняем загруженный текст в его состояние
                admin.setLoadFile(true);
                admin.setUploadText(false);
                return sendMessage.setText("Загрузите файл");
            } else {  //если админ должен загрузить только текст
                mainTelegramBot.execute(sendMessage.setText("Рассылка студентам началась!"));
                sendMessage.setText(text);
                //рассылка текста всем студентам которые есть в мэпе
                try {
                    if (admin.isVipSending()){
                        for (User user : telegramUsers.getUserMap().values()) {
                            if (user.getVip().equals("1")) {
                                sendMessage.setChatId(user.getChatId());
                                mainTelegramBot.execute(sendMessage);
                            }
                        }
                    }else {
                        for (User user : telegramUsers.getUserMap().values()) {
                            sendMessage.setChatId(user.getChatId());
                            mainTelegramBot.execute(sendMessage);
                        }
                    }
                    //рассылка текста текущему админу
                    sendMessage.setChatId(chatId);
                    mainTelegramBot.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    admin.statusFalse();
                    return sendMessage.setChatId(chatId).setText("Не удалось разослать текст!");
                }
                if (admin.isSendOtherHW()){
                    Date date =  new Date(update.getMessage().getDate() * 1000l);  // дата рассылки доп дз админом
                    telegramUsers.getMapDateOther().put(admin.getOtherHW(), date);  //добавляем номер доп дз с датой в мэп
                    sendMessage.setText("Домашнее задание " + admin.getHW() + " разослано успешно!");
                    admin.statusFalse();
                    return sendMessage.setChatId(chatId);
                }
                admin.statusFalse();
                return sendMessage.setChatId(chatId).setText("Рассылка текста прошла успешно!");
            }

        }
        if (telegramUsers.getAdminMap().containsKey(username) && telegramUsers.getAdminMap().get(username).isUploadFile()){
            Admin admin = telegramUsers.getAdminMap().get(username);

            SendMessage sendMessage = new SendMessage();
            long chatId = update.getMessage().getChatId();
            sendMessage.setChatId(chatId);

            String fileId = update.getMessage().getDocument().getFileId();
            System.out.println(update.getMessage());
            try {
                SendDocument sendDocument = new SendDocument();
                java.io.File file = telegramBotFile.getFile(fileId);  //получаем файл изображения

//                sendPhoto.setCaption(admin.getText());  //устанавливаем текст к изображению max 1024 символов
                sendDocument.setDocument(file);

                mainTelegramBot.execute(sendMessage.setText("Рассылка студентам началась!"));
                sendMessage.setText(admin.getText()).setChatId(chatId);

                try {
                    if (admin.isVipSending()) {
                        for (User user : telegramUsers.getUserMap().values()) {
                            if (user.getVip().equals("1")) {
                                sendDocument.setChatId(user.getChatId());
                                sendMessage.setChatId(user.getChatId());
                                mainTelegramBot.execute(sendMessage);
                                mainTelegramBot.execute(sendDocument);
                            }
                        }
                    } else {
                        for (User user : telegramUsers.getUserMap().values()) {
                            sendDocument.setChatId(user.getChatId());
                            sendMessage.setChatId(user.getChatId());
                            mainTelegramBot.execute(sendMessage);
                            mainTelegramBot.execute(sendDocument);
                        }
                    }

                    sendDocument.setChatId(chatId);
                    sendMessage.setChatId(chatId);
                    mainTelegramBot.execute(sendMessage);
                    mainTelegramBot.execute(sendDocument);
                    file.delete();

                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    admin.statusFalse();
                    file.delete();
                    return sendMessage.setText("Не удалось разослать файл с текстом!");
                }

            } catch (Exception e) {
                e.printStackTrace();
                admin.statusFalse();
                return sendMessage.setText("Не удалось загрузить файл!");
            }

            if (admin.isSendOtherHW()) {
                Date date = new Date(update.getMessage().getDate() * 1000l);  // дата рассылки доп дз админом
                telegramUsers.getMapDateOther().put(admin.getOtherHW(), date);  //добавляем номер доп дз с датой в мэп
                sendMessage.setText("Дополнительное домашнее задание " + admin.getOtherHW() + " разослано успешно!");
                admin.statusFalse();
                return sendMessage;
            }

            admin.statusFalse();
            return sendMessage.setText("Текст с файлом разосланы успешно!");
        }
        return new SendMessage().setChatId(update.getMessage().getChatId()).setText("Извините, но я вас не понимаю! ");
    }

    /**
     * обработка видео
     * @param update
     * @return ответ на пришедший update
     * @throws TelegramApiException
     */
    private BotApiMethod<?> eventHasVideo(Update update) throws TelegramApiException {
        String username = update.getMessage().getFrom().getUserName();
        //если это админ и он должен загрузит видео
        if (telegramUsers.getAdminMap().containsKey(username) && telegramUsers.getAdminMap().get(username).isUploadVideo()) {

            if (!telegramUsers.getAdminMap().get(username).isSendingVideo())
                 telegramBotFile.uploadVideo(mainTelegramBot, update, username);
            return null;

        }
        return new SendMessage().setChatId(update.getMessage().getChatId()).setText("Извините, но я вас не понимаю!");
    }

    /**
     * обработка картинок
     * @param update
     * @return ответ на пришедший update
     * @throws TelegramApiException
     */
    private BotApiMethod<?> eventHasPhoto(Update update) throws TelegramApiException {
        String username = update.getMessage().getFrom().getUserName();
        //если это админ и он должен загрузить изображение
        if (telegramUsers.getAdminMap().containsKey(username) && telegramUsers.getAdminMap().get(username).isUploadPhoto()) {
            Admin admin = telegramUsers.getAdminMap().get(username);

            SendMessage sendMessage = new SendMessage();
            long chatId = update.getMessage().getChatId();
            sendMessage.setChatId(chatId);

            String fileId = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1).getFileId();
            System.out.println(update.getMessage());
            try {
                SendPhoto sendPhoto = new SendPhoto();
                java.io.File file = telegramBotFile.getFile(fileId);  //получаем файл изображения

//                sendPhoto.setCaption(admin.getText());  //устанавливаем текст к изображению max 1024 символов
                sendPhoto.setPhoto(file);

                mainTelegramBot.execute(sendMessage.setText("Рассылка студентам началась!"));
                sendMessage.setText(admin.getText()).setChatId(chatId);

                try {
                    if (admin.isVipSending()) {
                        for (User user : telegramUsers.getUserMap().values()) {
                            if (user.getVip().equals("1")) {
                                sendPhoto.setChatId(user.getChatId());
                                sendMessage.setChatId(user.getChatId());
                                mainTelegramBot.execute(sendPhoto);
                                mainTelegramBot.execute(sendMessage);
                            }
                        }
                    } else {
                        for (User user : telegramUsers.getUserMap().values()) {
                            sendPhoto.setChatId(user.getChatId());
                            sendMessage.setChatId(user.getChatId());
                            mainTelegramBot.execute(sendPhoto);
                            mainTelegramBot.execute(sendMessage);
                        }
                    }

                    sendPhoto.setChatId(chatId);
                    sendMessage.setChatId(chatId);
                    mainTelegramBot.execute(sendPhoto);
                    mainTelegramBot.execute(sendMessage);
                    file.delete();

                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    admin.statusFalse();
                    file.delete();
                    return sendMessage.setText("Не удалось разослать картинку с текстом!");
                }

            } catch (Exception e) {
                e.printStackTrace();
                admin.statusFalse();
                return sendMessage.setText("Не удалось загрузить картинку!");
            }

            if (admin.isSendOtherHW()) {
                Date date = new Date(update.getMessage().getDate() * 1000l);  // дата рассылки доп дз админом
                telegramUsers.getMapDateOther().put(admin.getOtherHW(), date);  //добавляем номер доп дз с датой в мэп
                sendMessage.setText("Дополнительное домашнее задание " + admin.getOtherHW() + " разослано успешно!");
                admin.statusFalse();
                return sendMessage;
            }

            admin.statusFalse();
            return sendMessage.setText("Текст с фото разосланы успешно!");

        }
        //Если это студент
        if (telegramUsers.getUserMap().containsKey(username)) {
            User user = telegramUsers.getUserMap().get(username);

            String caption = update.getMessage().getCaption();
            System.out.println(caption);
            if (caption !=null && caption.toLowerCase().indexOf("#отчет") == 0) {
                String num = caption.substring(6);

                long chatId = update.getMessage().getChatId();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);

                user.statusFalse();

                mainTelegramBot.execute(sendMessage.setText("Дополнительное домашнее задание " + caption + " отправляется!✉️"));

                String fileId = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1).getFileId();
                File userFolder = user.getUserDirectory();  //получаем студенческую папку

                boolean sendHW = false;  //флаг проверки успешной отправки файла
                try (InputStream inputStream = telegramBotFile.getStreamFile(fileId)) {
                    String fileName = telegramBotFile.getPathFile(fileId);
                    sendHW = telegramBotGoogleDrive.sendHomework(  //возвращает true если файл был успешно отправлен
                            inputStream,
                            fileName,
                            "#отчет" + num,
                            userFolder);

                } catch (Exception e) {
                    e.getStackTrace();
                    return sendMessage.setText("Не удалось отправить дополнительное домашнее задание!⛔️\n" +
                            "Попробуйте еще раз или сразу рискните написать моему хозяину @MarkStav😉");
                }

                if (sendHW) {  //если дз было успешно отправлено студентом
                    Date date = new Date(update.getMessage().getDate() * 1000l);    //дата отправки дз студентом
                    String text = botMessage.cashOtherHW(telegramUsers, user, date, num);    //получение сообщения успешной отправки дз и изменение монет в таблице
                    if (text == null) return null;
                    return sendMessage.setText(text);
                }

                return sendMessage.setText("Не удалось отправить дополнительное домашнее задание!");
            }

            if(user.isSendHomework()){
                long chatId = update.getMessage().getChatId();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);

                user.statusFalse();

                mainTelegramBot.execute(sendMessage.setText("Домашнее задание " + user.getNumFile() + " отправляется!💌"));

                String fileId = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1).getFileId();
                File userFolder = user.getUserDirectory();  //получаем студенческую папку

                boolean sendHW = false;  //флаг проверки успешной отправки файла
                try (InputStream inputStream = telegramBotFile.getStreamFile(fileId)) {
                    String fileName = telegramBotFile.getPathFile(fileId);
                    sendHW = telegramBotGoogleDrive.sendHomework(  //возвращает true если файл был успешно отправлен
                            inputStream,
                            fileName,
                            "Домашнее задание " + user.getNumFile(),
                            userFolder);

                } catch (Exception e) {
                    e.getStackTrace();
                    return sendMessage.setText("Не удалось отправить домашнее задание!⛔️\n" +
                            "Попробуйте еще раз или сразу рискните написать моему хозяину @MarkStav😉");
                }

                if (sendHW) {  //если дз было успешно отправлено студентом
                    Date date = new Date(update.getMessage().getDate() * 1000l);    //дата отправки дз студентом
                    String text = botMessage.cashHW(telegramUsers, user, date);    //получение сообщения успешной отправки дз и изменение монет в таблице
                    if (text == null) return null;
                    return sendMessage.setText(text);
                }

                return sendMessage.setText("Не удалось отправить домашнее задание!⛔️\n" +
                        "Попробуйте еще раз или сразу рискните написать моему хозяину @MarkStav😉");
            }
        }
        return new SendMessage().setChatId(update.getMessage().getChatId()).setText("Извините, но я вас не понимаю!");
    }

    /**
     * обработка инлайновых кнопок
     * @param update
     * @return ответ на пришедший update
     */
    private BotApiMethod<?> eventInlineButton(Update update){
        String username = update.getCallbackQuery().getFrom().getUserName();
        //если это админ
        if (telegramUsers.getAdminMap().containsKey(username)) {
            Admin admin = telegramUsers.getAdminMap().get(username);
            String answer = "";
            String buttonId = update.getCallbackQuery().getData();  //получаем id кнопки

            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();  //получаем id сообщения к которой прикреплены кнопки
            long chatId = update.getCallbackQuery().getMessage().getChatId();  //получаем id чата

            EditMessageText editMessageText = new EditMessageText()  //создаем сообщение, которое заменит предыдущее
                    .setChatId(chatId)
                    .setMessageId(messageId);

            if (buttonId.startsWith("hw")) {  //если id кнопки начинаются с hw
                String num = update.getCallbackQuery().getData().substring(2);  //получаем номер домашки из id кнопки
                admin.setHW(num);  //устонавливаем номер домашки в состояние админа
                admin.setUploadText(true);  //состояние, отправка текста устанавливаем true
                admin.setUploadVideo(true);   //состояние, отправка видео устанавливаем true
                admin.setUploadPhoto(false);  //состояние, отправка изображения устанавливаем false
                return editMessageText.setText("Загрузите файл с текстом для дз"+ num);
            }
            switch (buttonId) {  //смотрим какой id у кнопки, то и выполняем
                case "text":
                    admin.setUploadText(true);
                    admin.setUploadVideo(false);
                    admin.setUploadPhoto(false);
                    if (admin.isSendOtherHW())
                        return editMessageText.setText("Введите номер дз");
                    return editMessageText.setText("Загрузите файл с текстом");
                case "textFile":
                    admin.setUploadText(true);
                    admin.setUploadFile(true);
                    admin.setUploadVideo(false);
                    admin.setUploadPhoto(false);
                    if (admin.isSendOtherHW())
                        return editMessageText.setText("Введите номер дз");
                    return editMessageText.setText("Загрузите файл с текстом");
                case "textVideo":
                    admin.setUploadText(true);
                    admin.setUploadVideo(true);
                    admin.setUploadPhoto(false);
                    if (admin.isSendOtherHW())
                        return editMessageText.setText("Введите номер дз");
                    return editMessageText.setText("Загрузите файл с текстом");
                case "textImage":
                    admin.setUploadText(true);
                    admin.setUploadPhoto(true);
                    admin.setUploadVideo(false);
                    if (admin.isSendOtherHW())
                        return editMessageText.setText("Введите номер дз");
                    return editMessageText.setText("Загрузите файл с текстом");
                case "main":
                    admin.statusFalse();
                    return editMessageText.setText("Выберите основное домашнее задание:").setReplyMarkup(TelegramButton.sendingAdminMainHW());
                case "other":
                    admin.statusFalse();
                    admin.setSendOtherHW(true);
                    return editMessageText.setText("Выберите дополнительное домашнее задание:").setReplyMarkup(TelegramButton.sendingAdminOtherHW());
                case "usual":
                    admin.statusFalse();
                    return editMessageText.setText("Выберите вид основной рассылки:").setReplyMarkup(TelegramButton.sending());
                case "vip":
                    admin.statusFalse();
                    admin.setVipSending(true);
                    return editMessageText.setText("Выберите вид vip рассылки:").setReplyMarkup(TelegramButton.sending());


            }
            return new SendMessage().setChatId(update.getMessage().getChatId()).setText("Извините, но я вас не понимаю!");
        }
        //если это студент
        if(telegramUsers.getUserMap().containsKey(username)) {

            telegramUsers.getUserMap().get(username).setSendHomework(true);   //состояние, отправка дз устанавливаем true

            String num = update.getCallbackQuery().getData().substring(2);  //получаем номер домашки
            telegramUsers.getUserMap().get(username).setNumFile(num);   //устанавливаем номер отправимого файла

            Integer message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            String answer = "Пожалуйста загрузите домашнее задание " + num + "📎";
            EditMessageText new_message = new EditMessageText()
                    .setChatId(chat_id)
                    .setMessageId(message_id)
                    .setText(answer);

            log.info("{} = {}", username, telegramUsers.getUserMap().get(username));

            return new_message;

        }
        return new SendMessage().setChatId(update.getMessage().getChatId()).setText("Извините, но я вас не понимаю🙀\n Нажмите /help и прочитайте, что я умею😸");
    }

    /**
     * создание ответа на пришедший update
     * @param update пришедший update от пользователя
     * @return  ответ на пришедший update
     */
    public BotApiMethod<?> createAnswer(Update update){

        if (update.hasCallbackQuery()){
            return eventInlineButton(update);
        }
        if (update.getMessage().hasDocument()) {
            try {
                return eventHasDocument(update);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if (update.getMessage().hasPhoto()){
            try {
                return eventHasPhoto(update);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        if (update.getMessage().hasVideo()) {
            try {
                return eventHasVideo(update);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if (update.getMessage().hasText()) {
            return eventHasText(update);
        }

        return null;
    }


}
