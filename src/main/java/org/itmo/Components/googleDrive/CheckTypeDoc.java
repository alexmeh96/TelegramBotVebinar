package org.itmo.Components.googleDrive;

import java.io.File;

// Проверка расширения файла(для загрузки на гугл диск
public class CheckTypeDoc {
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
            default:
                throw new IllegalStateException("Unexpected value: " + getFileExtension(fileName));
        }
    }
}
