package org.itmo.Components.service;

import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.Components.model.Question;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.itmo.MainTelegramBot;
import org.itmo.config.BotProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * текстовые сообщения
 */
@Component
public class BotMessage {
    @Autowired
    private MainTelegramBot mainTelegramBot;
    /**
     * просьба написать админу
     * @return текстовое сообщение
     */
    public String messageAdmin(){
        return "Напишите нашему\nадминистратору " + BotProperty.ADMIN;
    }

    /**
     * приветствие студенту
     * @param usernameSheets имя студента
     * @return текстовое сообщение
     */
    public static String welcomeMessage(String usernameSheets) {
        return "Привет, " + usernameSheets + "! Я - твой бот-помощник в игре \"Метод Плесовских\". \n" +
                "Я 24/7 на связи, поэтому ты в любой момент можете обратиться ко мне со своим вопросом.";
    }

    /**
     * сообщение незарегистрированному пользователю
     * @return текстовое сообщение
     */
    public String negativeMessage(){
        return "Привет, вы еще не зарегистрировались на курс \n" +
                "Если вы регистрировались на курс, напишите нашему администратору " + BotProperty.ADMIN;
    }

    /**
     * список вопросов студентов
     * @param telegramUsers пользователи бота
     * @param date текущая дата получения списка
     * @return текстовое сообщение
     */
    public String questionList(TelegramUsers telegramUsers, Date date){

        Date firstDate = new Date(date.getTime() - BotProperty.TIME_QUESTION);

        StringBuilder text = new StringBuilder();
        for (User user : telegramUsers.getUserMap().values()) {
            if (user.getListQuestion() != null) {
                int i = 0;
                while (i<user.getListQuestion().size() && user.getListQuestion().get(i).getDate().before(firstDate)){
                    i++;
                }

                if (i==user.getListQuestion().size())
                    user.setListQuestion(new ArrayList<>());
                else if (i>0){
                    text.append("<b>").append(user.getUsernameSheet()).append("</b>\n");
                    user.setListQuestion(user.getListQuestion().subList(i, user.getListQuestion().size()));
                    for (Question qu : user.getListQuestion()) {
                        text.append(qu.toString());
                    }
                }else{
                    text.append("<b>").append(user.getUsernameSheet()).append("</b>\n");
                    for (Question qu : user.getListQuestion()) {
                        text.append(qu.toString());
                    }
                }
            }
        }

        if(text.toString().isEmpty())
            text.append("Список вопросов пуст!");

        return text.toString();
    }

    /**
     * изменение баллов в зависимости от даты отправки дз студентом
     * @param telegramUsers  пользователи бота
     * @param user студент отправивший дз
     * @param date  дата отправки дз
     * @return текстовое сообщение
     */
    public String cashHW(TelegramUsers telegramUsers, User user, Date date){
        String num = user.getNumFile();
        Date firstDate = new Date(date.getTime()- BotProperty.TIME_HW);
        if(!user.getSendHW().contains(num) && telegramUsers.getMapDate().containsKey(num)){
            if (firstDate.before(telegramUsers.getMapDate().get(num))) {
                user.setCash(user.getCash() + BotProperty.CASH_HW);
                SendPhoto sendPhoto = new SendPhoto();   //получаем меню студента
                java.io.File file = new java.io.File("src/main/resources/img/win" + num + ".png");
                sendPhoto.setPhoto(file);
                String text = "Ваше домашнее задание отправлено вовремя!\nВы получаете " + BotProperty.CASH_HW + " монет!";
                sendPhoto.setCaption(text);
                sendPhoto.setChatId(user.getChatId());
                try {
                    mainTelegramBot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                user.getSendHW().add(num);
                try {
                    BotGoogleSheet.Update(BotProperty.SHEET_CASH_COL, user.getRowId(), String.valueOf(user.getCash()));
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
                return null;

            }else {
                SendPhoto sendPhoto = new SendPhoto();   //получаем меню студента
                java.io.File file = new java.io.File("src/main/resources/img/fail1.png");
                sendPhoto.setPhoto(file);
                if ((user.getCash() + BotProperty.MINUS_CASH_HW) >= 0)
                    user.setCash(user.getCash() + BotProperty.MINUS_CASH_HW);
                String text = "Ваше домашнее задание отправлено невовремя!\nВы получаете " + BotProperty.MINUS_CASH_HW + " монет!";
                sendPhoto.setCaption(text).setChatId(user.getChatId());
                try {
                    mainTelegramBot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                user.getSendHW().add(num);
                try {
                    BotGoogleSheet.Update(BotProperty.SHEET_CASH_COL, user.getRowId(), String.valueOf(user.getCash()));
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        return "Вы уже отправляли домашнее задание " + num + "!";
    }

    /**
     * изменение баллов в зависимости от даты отправки дополнительного дз студентом
     * @param telegramUsers пользователи бота
     * @param user студент отправивший дз
     * @param date дата отправки дз
     * @param num номер дополнительного дз
     * @return текстовое сообщение
     */
    public String cashOtherHW(TelegramUsers telegramUsers, User user, Date date, String num){

        Date firstDate = new Date(date.getTime() - BotProperty.TIME_OTHER_HW);
        if(!user.getSendOtherHW().contains(num) && telegramUsers.getMapDateOther().containsKey(num)){

            if (firstDate.before(telegramUsers.getMapDateOther().get(num))) {
                SendPhoto sendPhoto = new SendPhoto();   //получаем меню студента
                java.io.File file = new java.io.File("src/main/resources/img/win.png");
                sendPhoto.setPhoto(file);
                user.setCash(user.getCash() + BotProperty.CASH_OTHER_HW);
                String text = "Ваше дополнительное домашнее задание отправлено вовремя!\nВы получаете " + BotProperty.CASH_OTHER_HW + " монет!";
                sendPhoto.setCaption(text).setChatId(user.getChatId());
                try {
                    mainTelegramBot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                user.getSendOtherHW().add(num);
                try {
                    BotGoogleSheet.Update(BotProperty.SHEET_CASH_COL, user.getRowId(), String.valueOf(user.getCash()));
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
                return null;
            }else {
                SendPhoto sendPhoto = new SendPhoto();   //получаем меню студента
                java.io.File file = new java.io.File("src/main/resources/img/fail2.png");
                sendPhoto.setPhoto(file);
                if ((user.getCash() + BotProperty.MINUS_CASH_OTHER_HW) >= 0)
                    user.setCash(user.getCash() + BotProperty.MINUS_CASH_OTHER_HW);
                String text = "Ваше дополнительное домашнее задание отправлено невовремя!\nВы получаете " + BotProperty.MINUS_CASH_OTHER_HW + " монет!";
                sendPhoto.setCaption(text).setChatId(user.getChatId());
                try {
                    mainTelegramBot.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                user.getSendOtherHW().add(num);
                try {
                    BotGoogleSheet.Update(BotProperty.SHEET_CASH_COL, user.getRowId(), String.valueOf(user.getCash()));
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        return "Ваше дополнительное домашнее задание отправлено!";
    }

    /**
     * список топ студентов по колличеству монет
     * @param telegramUsers пользователи бота
     * @return текстовое сообщение
     */
    public String topUsers(TelegramUsers telegramUsers){
        List<User> userList = telegramUsers.getUserMap().values().stream().sorted((user, t1) -> t1.getCash() - user.getCash()).collect(Collectors.toList());

        StringBuilder result = new StringBuilder();

        for (User user : userList){
            result.append(user.getUsernameSheet()).append(" ").append(user.getCash()).append("\n");
        }

        return result.toString();
    }
}
