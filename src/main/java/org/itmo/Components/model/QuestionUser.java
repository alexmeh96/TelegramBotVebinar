package org.itmo.Components.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QuestionUser {
    private String text;
    private Date date;

    public QuestionUser(String text, Date date) {
        this.date = date;
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text + "\n<i>" + new SimpleDateFormat("dd.MM HH:mm:ss").format(date) + "</i>\n";
    }
}
