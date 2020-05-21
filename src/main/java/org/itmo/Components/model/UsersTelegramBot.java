package org.itmo.Components.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UsersTelegramBot {

    private Map<String, User> userMap= new HashMap<>();

    public Map<String, User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, User> userList) {
        this.userMap = userList;
    }
}
