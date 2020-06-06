package org.itmo.Components.model;

import lombok.Getter;
import lombok.Setter;

/**
 * класс хранящий состояние и данные админа
 */
@Getter
@Setter
public class Admin {

    private String name;    //ник админа в телеграме
    private Long chatId;    //Id чата
    private boolean uploadText;  //состояние загрузки текста
    private boolean uploadVideo;  //состояние загрузки видео
    private boolean uploadPhoto;   //состояние загрузки изображения
    private String HW;     //номер домашнего задания для рассылки
    private String text;  //загруженный текст для рассылки
    private String otherHW;  //номер дополнительного домашнего задания для рассылки
    private boolean sendOtherHW;  //состояние отправки дополнительного дз
    private boolean vipSending;

    public Admin(String name, Long chatId){
        this.name = name;
        this.chatId = chatId;
        text = "";
        HW = "";
        otherHW = "";
    }

    /**
     * сброс состояния админа
     */
    public void statusFalse(){
        text = "";
        uploadText=false;
        uploadVideo=false;
        uploadPhoto=false;
        HW = "";
        otherHW = "";
        sendOtherHW = false;
        vipSending = false;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "name='" + name + '\'' +
                ", chatId=" + chatId +
                ", uploadText=" + uploadText +
                ", uploadVideo=" + uploadVideo +
                ", uploadPhoto=" + uploadPhoto +
                ", HW='" + HW + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
