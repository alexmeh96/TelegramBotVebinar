package org.itmo.Components.model;

public class User {

    private String username;
    private String usernameSheet;
    private boolean sendHomework;

    public User(){}

    public User(String username, String usernameSheet) {
        this.username = username;
        this.usernameSheet = usernameSheet;
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
}
