package org.itmo.Components.service;

import lombok.Cleanup;
import org.itmo.Components.model.Admin;
import org.itmo.Components.model.TelegramUsers;
import org.itmo.Components.model.User;
import org.itmo.MainTelegramBot;
import org.itmo.config.BotProperty;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;

import java.net.URL;
import java.util.Date;

/**
 * обработка загруженного файла
 */
@Component
public class TelegramBotFile {

    @Autowired
    TelegramUsers telegramUsers;

    final String FILE_ID_URL = "https://api.telegram.org/bot" + BotProperty.TOKEN + "/getFile?file_id=";
    final String FILE_PATH_URL = "https://api.telegram.org/file/bot" + BotProperty.TOKEN + "/";


    /**
     * получение потока загруженного файла пользователем
     * @param fileId id загруженного файла
     * @return поток загруженного файла
     * @throws IOException
     */
    public InputStream getStreamFile(String fileId) throws IOException {
        String filePath = getPathFile(fileId);
        return new URL(FILE_PATH_URL + filePath).openStream();
    }

    /**
     * получение загруженного файл пользователем
     * @param fileId id загруженного файла
     * @return загруженный файл
     * @throws IOException
     */
    public File getFile(String fileId) throws IOException {

        String filePath = getPathFile(fileId);

        int index = filePath.lastIndexOf("/");
        String name = "";
        if (index == -1) name = filePath;
        else name = filePath.substring(index + 1);

        InputStream inputStream = new URL(FILE_PATH_URL + filePath).openStream();

        File file = new File(name);
        OutputStream outputStream = new FileOutputStream(file);

        int read = 0;
        byte[] bytes = new byte[inputStream.available()];
        while ((read = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }
        inputStream.close();
        outputStream.close();

        return file;
    }

    /**
     * получение текста загруженного файла пользователем
     * @param fileId id загруженного файла
     * @return текст файла
     * @throws IOException
     */
    public String getTextFile(String fileId) throws IOException {
        String text = "";
        String filePath = getPathFile(fileId);

        InputStream inputStream = new URL(FILE_PATH_URL + filePath).openStream();

        byte[] mas = new byte[inputStream.available()];
        inputStream.read(mas);

        text = new String(mas);
        inputStream.close();

        return text;
    }

    /**
     * возвращает путь файла на сервере телеграма
     * @param fileId id файла
     * @return путь файла на сервере телеграма
     * @throws IOException
     */
    public String getPathFile(String fileId) throws IOException {
        URL url = new URL(FILE_ID_URL + fileId);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        in.close();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        return path.getString("file_path");
    }

    @Async
    public void uploadVideo(MainTelegramBot mainTelegramBot, Update update, String username) throws TelegramApiException {
        Admin admin = telegramUsers.getAdminMap().get(username);
        admin.setSendingVideo(true);


        SendMessage sendMessage = new SendMessage();
        long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId);

        try {
            String fileId = update.getMessage().getVideo().getFileId();
//            java.io.File file = telegramBotFile.getFile(fileId);  //получаем файл с видео
            java.io.File file = getFile(fileId);  //получаем файл с видео
            SendVideo sendVideo = new SendVideo();
            sendVideo.setVideo(file);
            sendVideo.setWidth(600);
            sendVideo.setHeight(400);
//                sendVideo.setCaption(admin.getText());   // устанавливаем текст видео max 1024 символов

            mainTelegramBot.execute(sendMessage.setText("Рассылка студентам началась!"));
            sendMessage.setText(admin.getText()).setChatId(chatId);

            try {
                if (admin.isVipSending()) {
                    for (User user : telegramUsers.getUserMap().values()) {
                        if (user.getVip().equals("1")) {
                            sendVideo.setChatId(user.getChatId());
                            sendMessage.setChatId(user.getChatId());
                            mainTelegramBot.execute(sendVideo);
                            mainTelegramBot.execute(sendMessage);
                        }
                    }
                }else {
                    for (User user : telegramUsers.getUserMap().values()) {
                        sendVideo.setChatId(user.getChatId());
                        sendMessage.setChatId(user.getChatId());
                        mainTelegramBot.execute(sendVideo);
                        mainTelegramBot.execute(sendMessage);
                    }
                }

                sendVideo.setChatId(chatId);
                sendMessage.setChatId(chatId);
                mainTelegramBot.execute(sendVideo);
                mainTelegramBot.execute(sendMessage);
                file.delete();
            } catch (TelegramApiException e) {
                e.printStackTrace();
                admin.statusFalse();
                admin.setSendingVideo(false);
                file.delete();
                if (admin.getHW().isEmpty()) { //еслиадмин делает рассылку видео с текстом
                    mainTelegramBot.execute(sendMessage.setText("Не удалось разослать видео с текстом!"));
                    return;
                }
                else {  //еслиадмин делает рассылку дз
                    mainTelegramBot.execute(sendMessage.setText("Не удалось разослать домашнее задание " + admin.getHW()));
                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            admin.statusFalse();
            admin.setSendingVideo(false);
            mainTelegramBot.execute(sendMessage.setText("Не удалось загрузить видео!"));
            return ;
        }
        if (admin.isSendOtherHW()){
            Date date =  new Date(update.getMessage().getDate() * 1000l);  // дата рассылки доп дз админом
            telegramUsers.getMapDateOther().put(admin.getOtherHW(), date);  //добавляем номер доп дз с датой в мэп
            sendMessage.setText("Дополнительное домашнее задание " + admin.getOtherHW() + " разослано успешно!");
            admin.statusFalse();
            admin.setSendingVideo(false);
            mainTelegramBot.execute(sendMessage);
            return ;
        }

        if (admin.getHW().isEmpty()) {  //еслиадмин делает рассылку видео с текстом
            admin.statusFalse();
            admin.setSendingVideo(false);
            mainTelegramBot.execute(sendMessage.setText("Текст с видео разосланы успешно!"));
            return ;
        } else {    //еслиадмин делает рассылку дз
            Date date =  new Date(update.getMessage().getDate() * 1000l);  // дата рассылки дз админом
            telegramUsers.getMapDate().put(admin.getHW(), date);  //добавляем номер дз с датой в мэп
            sendMessage.setText("Домашнее задание " + admin.getHW() + " разослано успешно!");
            admin.statusFalse();
            admin.setSendingVideo(false);
            mainTelegramBot.execute(sendMessage);
            return ;
        }
    }

}
