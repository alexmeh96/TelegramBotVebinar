package org.itmo.Components.model;

import com.google.api.services.drive.model.File;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * класс хранящий состояние и данные студента
 */
@Getter
@Setter
public class User {

    private Long chatId;    //Id чата
    private String username;   //ник телеграма
    private String usernameSheet;   //имя
    private boolean sendHomework;  //состояние отправки дз
    private boolean sendOtherHomework;  //состояние отправки дз
    private boolean sendQuestion;   //состояние отправки вопроса
    private File userDirectory;   //папка
    private String numFile;  //номер отправимого файла
    private Integer cash;    //количество монет
    private Set<String> sendHW = new TreeSet<>();    //номера отправленных домашек
    private Set<String> sendOtherHW = new TreeSet<>();    //номера отправленных дополнительных домашек
    private String rowId;    //номер строки студента в созданной таблице
    private List<Question> listQuestion = new ArrayList<>();   //список вопросов
    private String vip;
    private Map<String, Integer> failedHW = new HashMap<>(); //мэп с неотпраленными основными дз и кол-вом  оповещений
    private Map<String, Integer> failedOtherHW = new HashMap<>(); //мэп с неотпраленными другими дз и кол-вом  оповещений


    public User(){}

    public User(Long chatId, String username, String usernameSheet, File file, String rowId) {
        this.chatId = chatId;
        this.username = username;
        this.usernameSheet = usernameSheet;
        this.userDirectory = file;
        this.cash = 0;
        this.rowId = rowId;
        this.vip = "0";
    }

    /**
     * сброс состояния админа
     */
    public void statusFalse(){
        sendHomework = false;
        sendOtherHomework = false;
        sendQuestion = false;
    }

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", username='" + username + '\'' +
                ", usernameSheet='" + usernameSheet + '\'' +
                ", sendHomework=" + sendHomework +
                ", sendQuestion=" + sendQuestion +
                ", userDirectory=" + userDirectory +
                ", numFile='" + numFile + '\'' +
                ", cash=" + cash +
                ", sendHW=" + sendHW +
                ", rowId='" + rowId + '\'' +
                ", listQuestion=" + listQuestion +
                '}';
    }
}
