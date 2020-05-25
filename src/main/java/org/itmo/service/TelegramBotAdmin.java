package org.itmo.service;

import org.springframework.stereotype.Service;

enum Question {
    USERNAME,
    PASSWORD,
}

@Service
public class TelegramBotAdmin {
    //private Long chatId;
    private String name;
    private String password;

    private Question question;
    private Boolean activate;

    private boolean correctPassword;
    private boolean correctUsername;

    public TelegramBotAdmin(){
        name = "root";
        password = "root";
        activate = false;
        question = Question.USERNAME;
    }

    public void authentication(String text){
        switch (question){
            case USERNAME:
                correctUsername = name.equals(text);
                question = Question.PASSWORD;
                break;
            case PASSWORD:
                correctPassword = password.equals(text);
                if (correctPassword && correctUsername)
                    activate = true;
                break;

        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getActivate() {
        return activate;
    }

    public void setActivate(Boolean activate) {
        this.activate = activate;
    }
}
