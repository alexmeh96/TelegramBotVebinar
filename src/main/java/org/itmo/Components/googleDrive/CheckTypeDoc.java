package org.itmo.Components.googleDrive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

// Проверка расширения файла(для загрузки на гугл диск
public class CheckTypeDoc {
    public static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotGoogleDrive.class);

    // вытаскиваем расширение
    private static String getFileExtension(String fileName) {
        // если в имени файла есть точка и она не является первым символом в названии файла
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".") + 1);
            // в противном случае возвращаем заглушку, то есть расширение не найдено
        else return "";
    }
    // делаем проверку и возвращаем шаблон
    public static String CheckType (String fileName){
        switch (getFileExtension(fileName)) {
            case "txt":
                return "text/plain";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "docx":
                return "application/vnd.oasis.opendocument.text";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/rar";
            case "tar":
                return "application/tar";
            case "jpeg":
            case "jpg":
                return "image/jpeg";
            case "png":
                return "image/png";
            default:
                LOGGER.error("Ошибка определения типа файла {}: {}", fileName, getFileExtension(fileName));
                throw new IllegalStateException("Ошибка типа: " + getFileExtension(fileName));
        }
    }
}
