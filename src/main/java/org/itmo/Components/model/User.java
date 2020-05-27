package org.itmo.Components.model;

import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.List;

public class User {

    private Long chatId;
    private String username;
    private String usernameSheet;
    private boolean sendHomework;
    private boolean askQuestion;
    private File userDirectory;
    private String numFile;

    private List<QuestionUser> listQuestion = new ArrayList<>();

    public User(){}

    public User(Long chatId, String username, String usernameSheet, File file) {
        this.chatId = chatId;
        this.username = username;
        this.usernameSheet = usernameSheet;
        this.userDirectory = file;
        this.sendHomework = false;
        this.askQuestion = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsernameSheet() {
        return usernameSheet;
    }

    public void setUsernameSheet(String usernameSheet) {
        this.usernameSheet = usernameSheet;
    }

    public boolean isSendHomework() {
        return sendHomework;
    }

    public void setSendHomework(boolean sendHomework) {
        this.sendHomework = sendHomework;
    }

    public File getUserDirectory() {
        return userDirectory;
    }

    public void setUserDirectory(File userDirectory) {
        this.userDirectory = userDirectory;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getNumFile() {
        return numFile;
    }

    public void setNumFile(String numFile) {
        this.numFile = numFile;
    }

    public boolean isAskQuestion() {
        return askQuestion;
    }

    public void setAskQuestion(boolean askQuestion) {
        this.askQuestion = askQuestion;
    }

    public List<QuestionUser> getListQuestion() {
        return listQuestion;
    }

    public void setListQuestion(List<QuestionUser> listQuestion) {
        this.listQuestion = listQuestion;
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
                ", listQuestion=" + listQuestion +
                '}';
    }
}
