package org.itmo.Components.botFile;

import lombok.Cleanup;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

import java.net.URL;

/**
 * обработка загруженного файла
 */
@Component
public class TelegramBotFile {

    //@Value("${botToken}")
    private final String token = "1158197395:AAGIe0V25U0FgH9SuYkuFfz80EYii76cd7Q";

    final String FILE_ID_URL = "https://api.telegram.org/bot" + token + "/getFile?file_id=";
    final String FILE_PATH_URL = "https://api.telegram.org/file/bot" + token + "/";


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
}
