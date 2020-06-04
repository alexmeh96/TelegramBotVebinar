package org.itmo.Components.model;

import com.google.api.services.drive.model.File;
import lombok.Getter;
import lombok.Setter;
import org.itmo.Components.botButton.TelegramButton;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
public class User {

    private Long chatId;    //Id чата
    private String username;   //ник телеграма
    private String usernameSheet;   //имя
    private boolean sendHomework;
    private boolean askQuestion;
    private File userDirectory;   //папка
    private String numFile;
    private Integer cash;    //количество монет
    private Set<String> sendHW = new TreeSet<>();    //номера отправленных домашек
    private String rowId;    //номер строки студента в созданной таблице
    private List<Question> listQuestion = new ArrayList<>();   //список вопросов

    public User(){}

    public User(Long chatId, String username, String usernameSheet, File file, String rowId) {
        this.chatId = chatId;
        this.username = username;
        this.usernameSheet = usernameSheet;
        this.userDirectory = file;
        this.sendHomework = false;
        this.askQuestion = false;
        this.cash = 0;
        this.rowId = rowId;
    }


    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", username='" + username + '\'' +
                ", usernameSheet='" + usernameSheet + '\'' +
                ", sendHomework=" + sendHomework +
                ", askQuestion=" + askQuestion +
                ", userDirectory=" + userDirectory +
                ", numFile='" + numFile + '\'' +
                ", cash=" + cash +
                ", listQuestion=" + listQuestion +
                '}';
    }
}
