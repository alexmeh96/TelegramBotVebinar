package org.itmo.Components;

import org.itmo.Components.model.QuestionUser;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;

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

    public String questionList(TelegramUsers telegramUsers, Date date){
        //Date firstDate = new Date(date.getTime() - 172_800_000L);
        Date firstDate = new Date(date.getTime() - 20_000L);

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
                    text = new StringBuilder("<b>@" + user.getUsername() + "</b>\n");
                    user.setListQuestion(user.getListQuestion().subList(i, user.getListQuestion().size()));
                    for (QuestionUser qu : user.getListQuestion()) {
                        text.append(qu.toString());
                    }
                }else{
                    text = new StringBuilder("<b>@" + user.getUsername() + "</b>\n");
                    for (QuestionUser qu : user.getListQuestion()) {
                        text.append(qu.toString());
                    }
                }
            }
        }

        if(text.toString().isEmpty())
            text.append("спмсок вопросов пуст!");

        return text.toString();
    }

    public String cashFoHW(TelegramUsers telegramUsers, User user, Date date){
        String num = user.getNumFile();
        Date firstDate = new Date(date.getTime()-40_000l);
        if(telegramUsers.getMapDate().containsKey(num) && firstDate.before(telegramUsers.getMapDate().get(num))){
            user.setCash(user.getCash() + 10);
            return "Ваше домашнее задание отправлено вовремя!\nВы получаете плюс 10 баллов!";
        }
        return "Ваше домашнее задание отправлено!";
    }

    public String topUsers(TelegramUsers telegramUsers){
        
        return "";
    }
}
