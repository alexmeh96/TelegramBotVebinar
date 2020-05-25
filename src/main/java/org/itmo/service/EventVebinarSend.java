package org.itmo.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EventVebinarSend {


    @Scheduled(fixedRate = 500000)
    public void reportCurrentTime() {
        System.out.println("event");
    }

}
