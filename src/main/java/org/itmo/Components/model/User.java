package org.itmo.Components.model;

import com.google.api.services.drive.model.File;

public class User {

    private boolean admin;
    private String username;
    private String usernameSheet;
    private boolean sendHomework;
    private File userDirectory;

    public User(){}

    public User(String username, String usernameSheet, File file, boolean admin) {
        this.username = username;
        this.usernameSheet = usernameSheet;
        this.userDirectory = file;
        this.sendHomework = false;
        this.admin = admin;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(String role) {
        if (role != null && role.equals("admin"))
            this.admin = true;
        else
            this.admin = false;
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
                "admin=" + admin +
                ", username='" + username + '\'' +
                ", usernameSheet='" + usernameSheet + '\'' +
                ", sendHomework=" + sendHomework +
                ", userDirectory=" + userDirectory +
                '}';
    }
}
