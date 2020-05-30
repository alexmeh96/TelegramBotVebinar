package org.itmo.Components.botFile;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

@Component
public class TelegramBotFile {

    @Value("${botToken}")
    private String token;

    //возврщает поток загруженного пользователем файла
    public InputStream uploadUserFile(String file_id) throws IOException {
        final String FILE_ID_URL = "https://api.telegram.org/bot" + token + "/getFile?file_id=";
        final String FILE_PATH_URL = "https://api.telegram.org/file/bot" + token + "/";
        InputStream inputStream = null;

        URL url = new URL(FILE_ID_URL + file_id);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        in.close();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        String file_path = path.getString("file_path");

        inputStream = new URL(FILE_PATH_URL + file_path).openStream();
        in.close();

        return inputStream;
    }

    public String getTextFile(String fileId) throws IOException {
        final String FILE_ID_URL = "https://api.telegram.org/bot" + token + "/getFile?file_id=";
        final String FILE_PATH_URL = "https://api.telegram.org/file/bot" + token + "/";
        InputStream inputStream = null;
        String text = "";

        URL url = new URL(FILE_ID_URL + fileId);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        in.close();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        String file_path = path.getString("file_path");

        inputStream = new URL(FILE_PATH_URL + file_path).openStream();

        byte[] mas = new byte[inputStream.available()];
        inputStream.read(mas);

        text = new String(mas);
        inputStream.close();
        in.close();

        return text;
    }
}
