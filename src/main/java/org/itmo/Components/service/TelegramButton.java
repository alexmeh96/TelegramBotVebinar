package org.itmo.Components.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * управление кнопками
 */
@Component
public class TelegramButton {

    /**
     * Создание инлайновых кнопок
     * @param buttonListText  список названий кнопок
     * @param buttonListId  список id кнопок
     * @return инлайновая клавиатура
     */
    public static InlineKeyboardMarkup createInlineButton(List<String> buttonListText, List<String> buttonListId){
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (int i = 0; i < buttonListText.size(); i++) {
            rowInline.add(new InlineKeyboardButton().setText(buttonListText.get(i)).setCallbackData(buttonListId.get(i)));
        }

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    /**
     * создание статических кнопок
     * @param textMessage текстовое сообщение
     * @param buttonListText  список названий кнопок
     * @return  SendMessage с статическими кнопкими
     */
    public static SendMessage createButton(String textMessage, List<String> buttonListText) {
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String text : buttonListText){
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton().setText(text));
            keyboard.add(row);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);

        final SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setText(textMessage);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    /**
     * создание статических кнопок
     * @param buttonListText  список названий кнопок
     * @return  SendMessage с статическими кнопкими
     */
    public static SendPhoto createButtonPhoto(List<String> buttonListText) {
        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String text : buttonListText){
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton().setText(text));
            keyboard.add(row);
        }

        replyKeyboardMarkup.setKeyboard(keyboard);

        final SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setReplyMarkup(replyKeyboardMarkup);

        return sendPhoto;
    }

    /**
     * меню студента
     * @param message текстовое сообщение
     * @return SendMessage с статическими кнопкими
     */
    public static SendMessage userMenu(String message){
        List<String> stringList = new ArrayList<>();
        stringList.add("Отправить домашнее задание📚");
        stringList.add("Помощь🆘");
        stringList.add("Пароль от личного кабинета🤫");
        stringList.add("Рейтинг студентов🥇");

        return TelegramButton.createButton(message, stringList);
    }

    public static SendPhoto userMenuPhoto(){
        List<String> stringList = new ArrayList<>();
        stringList.add("Отправить домашнее задание📚");
        stringList.add("Помощь🆘");
        stringList.add("Пароль от личного кабинета🤫");
        stringList.add("Рейтинг студентов🥇");

        return TelegramButton.createButtonPhoto(stringList);
    }

    /**
     * админское меню
     * @return SendMessage с инлайновыми кнопкими
     */
    public static SendMessage adminMenu(){
        List<String> stringList = new ArrayList<>();
        stringList.add("Список вопросов");
        stringList.add("Сделать рассылку");
        stringList.add("Сделать рассылку дз");
        stringList.add("Обновить студентов");
        return TelegramButton.createButton("Здравствуйте администратор!",stringList);
    }

    /**
     * инлайновым меню рассылки
     * @return SendMessage с инлайновыми кнопкими
     */
    public static InlineKeyboardMarkup sending(){
        List<String> stringList = new ArrayList<>();
        stringList.add("текст");
        stringList.add("текст и видео");
        stringList.add("текст и картинка");

        List<String> stringId = new ArrayList<>();
        stringId.add("text");
        stringId.add("textVideo");
        stringId.add("textImage");

        return TelegramButton.createInlineButton(stringList, stringId);
    }

    public static SendMessage sendingChoose(){
        List<String> stringList = new ArrayList<>();
        stringList.add("Обычная");
        stringList.add("Vip");

        List<String> stringId = new ArrayList<>();
        stringId.add("usual");
        stringId.add("vip");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(TelegramButton.createInlineButton(stringList, stringId));
        sendMessage.setText("Выберите вид рассылки:");
        return sendMessage;
    }


    /**
     * инлайновое меню отправки дз
     * @return SendMessage с инлайновыми кнопкими
     */
    public static SendMessage sendingMainHW(){
        List<String> stringList = new ArrayList<>();
        stringList.add("дз1");
        stringList.add("дз2");
        stringList.add("дз3");
        stringList.add("дз4");

        List<String> stringId = new ArrayList<>();
        stringId.add("hw1");
        stringId.add("hw2");
        stringId.add("hw3");
        stringId.add("hw4");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(TelegramButton.createInlineButton(stringList, stringId));
        sendMessage.setText("Выберите домашнее задание⬇️:");
        return sendMessage;
    }

    /**
     * инлайновое меню вида дз
     * @return SendMessage с инлайновыми кнопкими
     */
    public static SendMessage sendingHW() {
        List<String> stringList = new ArrayList<>();
        stringList.add("основное");
        stringList.add("дополнительное");

        List<String> stringId = new ArrayList<>();
        stringId.add("main");
        stringId.add("other");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(TelegramButton.createInlineButton(stringList, stringId));
        sendMessage.setText("Выберите вид домашнего задания:");
        return sendMessage;
    }

    /**
     * Меню выбора обычного дз
     * @return инлайновые кнопки
     */
    public static InlineKeyboardMarkup sendingAdminMainHW(){
        List<String> stringList = new ArrayList<>();
        stringList.add("дз1");
        stringList.add("дз2");
        stringList.add("дз3");
        stringList.add("дз4");

        List<String> stringId = new ArrayList<>();
        stringId.add("hw1");
        stringId.add("hw2");
        stringId.add("hw3");
        stringId.add("hw4");

        return TelegramButton.createInlineButton(stringList, stringId);
    }

    /**
     * Меню выбора дополнительного дз
     * @return инлайновые кнопки
     */
    public static InlineKeyboardMarkup sendingAdminOtherHW(){
        List<String> stringList = new ArrayList<>();
        stringList.add("текст");
        stringList.add("текст и видео");
        stringList.add("текст и картинка");

        List<String> stringId = new ArrayList<>();
        stringId.add("text");
        stringId.add("textVideo");
        stringId.add("textImage");

        return TelegramButton.createInlineButton(stringList, stringId);
    }


}
