package org.itmo.Components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotMessage {

    @Value("${botAdmin}")
    private String botAdmin;
    @Value("${botSpiker}")
    private String botSpiker;

    public String messageAdmin(){
        return "Напишите нашему\nадминистратору " + botAdmin;
    }

    public String messageSpiker(){
        return "Напишите нашему\nспикеру " + botSpiker;
    }

    public String welcomeMessage(String usernameSheets) {
        return "Привет, " + usernameSheets + "! Я бот помощник.\n" +
                " - буду держать вас в курсе всех  ивентов вебинара\n" +
                " - отправлю и проверю ваше дз\n" +
                " - если есть вопросы помогу связаться с администратором или спикером" +
                " - напомню ваш пароль";

    }

    public String negativeMessage(){
        return "Привет, вы еще не зарегистрировались на курс \n" +
                "Если вы регистрировались на курс, напишите нашему администратору " + botAdmin;
    }
}
