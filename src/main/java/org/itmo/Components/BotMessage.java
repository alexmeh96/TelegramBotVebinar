package org.itmo.Components;

import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.itmo.Components.model.QuestionUser;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

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
        return "Привет, " + usernameSheets + "! Я - твой бот-помощник в игре \"Метод Плесовских\". \n" +
                "Я 24/7 на связи, поэтому ты в любой момент можете обратиться ко мне со своим вопросом.";

    }

    public String negativeMessage(){
        return "Привет, вы еще не зарегистрировались на курс \n" +
                "Если вы регистрировались на курс, напишите нашему администратору " + botAdmin;
    }

    public String questionList(TelegramUsers telegramUsers, Date date){
        //Date firstDate = new Date(date.getTime() - 172_800_000L);
        Date firstDate = new Date(date.getTime() - 40_000L);

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
            text.append("Список вопросов пуст!");

        return text.toString();
    }

    public String cashHW(TelegramUsers telegramUsers, User user, Date date){
        String num = user.getNumFile();
        Date firstDate = new Date(date.getTime()- 60_000l);
        if(!user.getSendHW().contains(num) && telegramUsers.getMapDate().containsKey(num) && firstDate.before(telegramUsers.getMapDate().get(num))){
            user.setCash(user.getCash() + 10);
            user.getSendHW().add(num);
            System.out.println(num);
            try {
                BotGoogleSheet.Update(7, user.getRowId(), String.valueOf(user.getCash()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
            return "Ваше домашнее задание отправлено вовремя!\nВы получаете 10 баллов!";
        }
        return "Ваше домашнее задание отправлено!";
    }

    public String topUsers(TelegramUsers telegramUsers){
        List<User> userList = telegramUsers.getUserMap().values().stream().sorted(new Comparator<User>() {
            @Override
            public int compare(User user, User t1) {
                return user.getCash() - t1.getCash();
            }
        }).collect(Collectors.toList());

        StringBuilder result = new StringBuilder();

        for (User user : userList){
            result.append(user.getUsername() + " " + user.getCash() + "\n");
        }

        return result.toString();
    }
}
