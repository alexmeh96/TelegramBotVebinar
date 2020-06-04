package org.itmo.Components.model;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class Question {
    private String text;    //текст вопроса
    private Date date;    //дата отправки вопроса

    public Question(String text, Date date) {
        this.date = date;
        this.text = text;
    }

    @Override
    public String toString() {
        return text + "\n<i>" + new SimpleDateFormat("dd.MM HH:mm:ss").format(date) + "</i>\n";
    }
}
