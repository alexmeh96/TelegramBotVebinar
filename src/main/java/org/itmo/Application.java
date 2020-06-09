package org.itmo;

import lombok.extern.slf4j.Slf4j;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SuppressWarnings("ALL")
@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application {
    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(BotGoogleSheet.class);
    public static void main(String[] args) {

//        System.getProperties().put("proxySet", "true");
//        System.getProperties().put("socksProxyHost", "127.0.0.1");
//        System.getProperties().put("socksProxyPort", "9150");
        SpringApplication.run(Application.class);
        log.info("БОТ АКТИВИРОВАН!");
    }
}
