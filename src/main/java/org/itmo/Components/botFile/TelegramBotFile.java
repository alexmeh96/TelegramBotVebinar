package org.itmo.Components.botFile;

import org.itmo.Components.googleDrive.TelegramBotGoogleDrive;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

@Component
public class TelegramBotFile {

    public static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotFile.class);

    @Value("${botToken}")
    private String token;

    //возврщает поток загруженного пользователем файла
    public InputStream uploadUserFile(String file_name, String file_id){
        final String FILE_ID_URL = "https://api.telegram.org/bot" + token + "/getFile?file_id=";
        final String FILE_PATH_URL = "https://api.telegram.org/file/bot" + token + "/";
        InputStream inputStream = null;
        try {
            URL url = new URL(FILE_ID_URL + file_id);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String res = in.readLine();
            in.close();
            JSONObject jresult = new JSONObject(res);
            JSONObject path = jresult.getJSONObject("result");
            String file_path = path.getString("file_path");

            inputStream = new URL(FILE_PATH_URL + file_path).openStream();

            LOGGER.info("Получено тело сообщения(ивента) {}", file_name);
            System.out.println(inputStream);

        }catch (Exception e){
            LOGGER.trace("Ошибка получения тела сообщения(ивента){} : {}", file_name, e.getStackTrace());
//            e.printStackTrace();
        }

        return inputStream;
    }
}
