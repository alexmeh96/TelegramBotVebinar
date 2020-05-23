package org.itmo.Components.model;

import com.google.api.services.drive.model.File;

public class User {

    private String username;
    private String usernameSheet;
    private boolean sendHomework;
    private File userDirectory;

    public User(){}

    public User(String username, String usernameSheet, File file) {
        this.username = username;
        this.usernameSheet = usernameSheet;
        this.userDirectory = file;
        this.sendHomework = false;
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

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", usernameSheet='" + usernameSheet + '\'' +
                ", sendHomework=" + sendHomework +
                '}';
    }
}
