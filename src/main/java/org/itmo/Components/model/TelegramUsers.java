package org.itmo.Components.model;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TelegramUsers {

    private Map<String, Date> mapDate = new HashMap<>();

    private Map<String, User> userMap= new HashMap<>();

    private List<String> adminList = new ArrayList<>();

    public Map<String, User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, User> userList) {
        this.userMap = userList;
    }

    public List<String> getAdminList() {
        return adminList;
    }

    public void setAdminList(List<String> adminList) {
        this.adminList = adminList;
    }

    public Map<String, Date> getMapDate() {
        return mapDate;
    }

    public void setMapDate(Map<String, Date> mapDate) {
        this.mapDate = mapDate;
    }
}
