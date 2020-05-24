package org.itmo.config;

import org.itmo.MainTelegramBot;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;

@Configuration
@ConfigurationProperties(prefix = "telegrambot")
public class TelegramBotConfig {
    private String webHookPath;
    private String botUserName;
    private String botToken;

    private DefaultBotOptions.ProxyType proxyType;
    private String proxyHost;
    private int proxyPort;

    @Bean
    public MainTelegramBot MySuperTelegramBot() {

        DefaultBotOptions options = ApiContext
                .getInstance(DefaultBotOptions.class);

        options.setProxyHost(proxyHost);
        options.setProxyPort(proxyPort);
        options.setProxyType(proxyType);
//
        MainTelegramBot mySuperTelegramBot = new MainTelegramBot(options);

     //   MainTelegramBot mySuperTelegramBot = new MainTelegramBot();
        mySuperTelegramBot.setBotUserName(botUserName);
        mySuperTelegramBot.setBotToken(botToken);
        mySuperTelegramBot.setWebHookPath(webHookPath);

        return mySuperTelegramBot;
    }

    public String getWebHookPath() {
        return webHookPath;
    }

    public void setWebHookPath(String webHookPath) {
        this.webHookPath = webHookPath;
    }

    public String getBotUserName() {
        return botUserName;
    }

    public void setBotUserName(String botUserName) {
        this.botUserName = botUserName;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public DefaultBotOptions.ProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(DefaultBotOptions.ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}