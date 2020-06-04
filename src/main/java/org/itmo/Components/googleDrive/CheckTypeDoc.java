package org.itmo.Components.googleDrive;


/**
 * Проверка расширения файла для запроса в Google Диск
 */
public class CheckTypeDoc {
    /**
     *  Метод для получения расширения файла
     * @param fileName имя файла
     * @return расширение файла
     */
    private static String getFileExtension(String fileName) {
        // если в имени файла есть точка и она не является первым символом в названии файла
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".") + 1);
            // в противном случае возвращаем заглушку, то есть расширение не найдено
        else return "";
    }
    // делаем проверку и возвращаем шаблон

    /**
     * Метод возвращающий тип Google файла
     * @param fileName имя файла
     * @return расширение Google файла
     */
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
                throw new IllegalStateException("Unexpected value: " + getFileExtension(fileName));
        }
    }
}
