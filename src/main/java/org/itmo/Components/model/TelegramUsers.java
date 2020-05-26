package org.itmo.Components.model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramUsers {

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
}
