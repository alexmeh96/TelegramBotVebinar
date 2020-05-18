package org.itmo.Components;

import org.springframework.stereotype.Component;

@Component
public class BotMessage {
    public String messageAdmin(){
        return "Напишите нашему\nадминистратору @MarkStav";
    }

    public String messageSpiker(){
        return "Напишите нашему\nспикеру @Gleb";
    }
}
