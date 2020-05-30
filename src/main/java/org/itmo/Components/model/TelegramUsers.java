package org.itmo.Components.model;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TelegramUsers {

    private Map<String, Date> mapDate = new HashMap<>();

    private Map<String, User> userMap= new HashMap<>();

    private Map<String, Admin> adminMap= new HashMap<>();

    public Map<String, User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, User> userList) {
        this.userMap = userList;
    }

    public Map<String, Date> getMapDate() {
        return mapDate;
    }

    public void setMapDate(Map<String, Date> mapDate) {
        this.mapDate = mapDate;
    }

    public Map<String, Admin> getAdminMap() {
        return adminMap;
    }

    public void setAdminMap(Map<String, Admin> adminMap) {
        this.adminMap = adminMap;
    }
}
