package org.itmo.Components.botFile;

import lombok.Cleanup;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Component
public class TelegramBotFile {

    @Value("${botToken}")
    private String token;

    //возврщает поток загруженного пользователем файла
    public InputStream uploadUserFile(String file_id) throws IOException {
        final String FILE_ID_URL = "https://api.telegram.org/bot" + token + "/getFile?file_id=";
        final String FILE_PATH_URL = "https://api.telegram.org/file/bot" + token + "/";

        URL url = new URL(FILE_ID_URL + file_id);

         BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        in.close();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        String file_path = path.getString("file_path");

        InputStream inputStream = new URL(FILE_PATH_URL + file_path).openStream();
        in.close();

        return inputStream;
    }

    public File uploadUserFile2(String file_id) throws IOException {
        final String FILE_ID_URL = "https://api.telegram.org/bot" + token + "/getFile?file_id=";
        final String FILE_PATH_URL = "https://api.telegram.org/file/bot" + token + "/";

        URL url = new URL(FILE_ID_URL + file_id);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        in.close();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        String file_path = path.getString("file_path");
        System.out.println(file_path);
        in.close();

        InputStream inputStream = new URL(FILE_PATH_URL + file_path).openStream();

        int index = file_path.lastIndexOf("/");
        String name = "";
        if (index == -1) name = file_path;
        else name = file_path.substring(index + 1);

        File file = new File(name);
        OutputStream outputStream = new FileOutputStream(file);

        int read = 0;
        byte[] bytes = new byte[inputStream.available()];
        while ((read = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }
        return file;
    }

    public String getTextFile(String fileId) throws IOException {
        final String FILE_ID_URL = "https://api.telegram.org/bot" + token + "/getFile?file_id=";
        final String FILE_PATH_URL = "https://api.telegram.org/file/bot" + token + "/";
        String text = "";

        URL url = new URL(FILE_ID_URL + fileId);

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        in.close();
        JSONObject jresult = new JSONObject(res);
        JSONObject path = jresult.getJSONObject("result");
        String file_path = path.getString("file_path");

        InputStream inputStream = new URL(FILE_PATH_URL + file_path).openStream();

        byte[] mas = new byte[inputStream.available()];
        inputStream.read(mas);

        text = new String(mas);
        inputStream.close();
         in.close();

        return text;
    }
}
