package org.itmo.Components.model;

public class Admin {
    private String name;
    private boolean uploadText;
    private boolean uploadVideo;
    private boolean uploadPhoto;
    private String text;

    public Admin(String name){
        this.name = name;
        text = "";
        uploadText = false;
        uploadVideo = false;
        uploadPhoto = false;
    }

    public void uploadFalse(){
        text = "";
        uploadText=false;
        uploadVideo=false;
        uploadPhoto=false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUploadText() {
        return uploadText;
    }

    public void setUploadText(boolean uploadText) {
        this.uploadText = uploadText;
    }

    public boolean isUploadVideo() {
        return uploadVideo;
    }

    public void setUploadVideo(boolean uploadVideo) {
        this.uploadVideo = uploadVideo;
    }

    public boolean isUploadPhoto() {
        return uploadPhoto;
    }

    public void setUploadPhoto(boolean uploadPhoto) {
        this.uploadPhoto = uploadPhoto;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
