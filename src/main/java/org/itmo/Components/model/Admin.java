package org.itmo.Components.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Admin {
    private String name;    //ник админа в телеграме
    private Long chatId;    //Id чата
    private boolean uploadText;
    private boolean uploadVideo;
    private boolean uploadPhoto;
    private String HW;
    private String text;

    public Admin(String name, Long chatId){
        this.name = name;
        this.chatId = chatId;
        text = "";
        uploadText = false;
        uploadVideo = false;
        uploadPhoto = false;
        HW = "";
    }

    public void uploadFalse(){
        text = "";
        uploadText=false;
        uploadVideo=false;
        uploadPhoto=false;
        HW = "";
    }

   }
