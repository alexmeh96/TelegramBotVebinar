package org.itmo.Components.botButton;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramButton {

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

    public static SendMessage createUserMenu( String message){
        List<String> stringList = new ArrayList<>();
        stringList.add("Отправить домашнее задание");
        stringList.add("Связаться со службой поддержки");
        stringList.add("Пароль от личного кабинета");
        stringList.add("Рейтинг студентов");

        return TelegramButton.createButton(message, stringList);
    }

    public static SendMessage createAdminMenu(){
        List<String> stringList = new ArrayList<>();
        stringList.add("Список вопросов");
        stringList.add("Сделать рассылку");
        stringList.add("Сделать рассылку дз");
        stringList.add("Обновить студентов");
        return TelegramButton.createButton("Здравствуйте администратор!",stringList);
    }

    public static SendMessage createSending(){
        List<String> stringList = new ArrayList<>();
        stringList.add("текст");
        stringList.add("текст и видео");
        stringList.add("текст и картинка");

        List<String> stringId = new ArrayList<>();
        stringId.add("text");
        stringId.add("textVideo");
        stringId.add("textImage");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(TelegramButton.createInlineButton(stringList, stringId));
        sendMessage.setText("Выберите тип рассылки:");
        return sendMessage;
    }

    public static SendMessage createSendingHW(){
        List<String> stringList = new ArrayList<>();
        stringList.add("дз1");
        stringList.add("дз2");
        stringList.add("дз3");

        List<String> stringId = new ArrayList<>();
        stringId.add("hw1");
        stringId.add("hw2");
        stringId.add("hw3");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(TelegramButton.createInlineButton(stringList, stringId));
        sendMessage.setText("Выберите домашнее задание:");
        return sendMessage;
    }
}
