package org.itmo.Components.googleDrive;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Component
public class TelegramBotGoogleDrive {
    // Параметры
    public  String PROJECT_NAME_FOLDER = "Проект 1";
    public  String NEW_NAME_FILE = "дз1";
    public  String STUDENT_NAME_FOLDER = "Иван Иванов";
    public  String IventID;
    public  String STUDENT_FOLDER_ID;
    // заглушка для теста с файлом на пк
//    public java.io.File UPLOAD_FILE = new java.io.File("/home/alex/work/java/Projects/TelegramBotVebinar/src/main/resources/test.zip");

    File folder_hw;
    File folder_student;

//    public TelegramBotGoogleDrive(){
//        deploy();
//    }

    //-------------------------------------ЗАПУСК ОДИН РАЗ СРАЗУ ПОСЛЕ ДЕПЛОЯ -------------------------
    public void deploy() {
        try {
            // Создание папки проекта
            File folder_project = CreateFolder.createGoogleFolder(null, PROJECT_NAME_FOLDER);
            // Создание папки с домашними заданиями всех пользователей
            folder_hw = CreateFolder.createGoogleFolder(folder_project.getId(), "Папка с дз");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------ЗАПУСК ОДИН РАЗ СРАЗУ ПОСЛЕ АКТИВАЦИИ БОТА -----------------
    public void activate(String username) {
        // Создание папки для студента
        //find user_sheet_name == true

        //else
        try {
            folder_student = CreateFolder.createGoogleFolder(folder_hw.getId(), username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -----------------ЗАПУСК КАЖДЫЙ РАЗ ПОСЛЕ АКТИВАЦИИ КНОПКИ "Отправить дз -> Дз1" -----------------
    public String sendHomework() {
        System.out.println("sendHomework");
        // Создание файлов с дз
        File googleFile = null;
        try {
            java.io.File UPLOAD_FILE = new java.io.File("/home/alex/work/java/Projects/TelegramBotVebinar/src/main/resources/test.zip");
            ////System.getProperty("user.home")+
            googleFile = CreateHWFile.createGoogleFile(folder_student.getId(), CheckTypeDoc.CheckType(UPLOAD_FILE), NEW_NAME_FILE, UPLOAD_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Отправить пользователю
        return "Ссылка для просмотра: " + googleFile.getWebViewLink();

    }
    // Поиск папки студента
    public boolean findDirectory(String userNameSheet){
        try {
            //прочекать про одинаковые дирректории
            List<File> rootGoogleFolders = GetSubFoldersByName.getGoogleRootFoldersByName(userNameSheet);
            System.out.println(userNameSheet);
            System.out.println(rootGoogleFolders);
            for (File folder : rootGoogleFolders) {
                STUDENT_FOLDER_ID = folder.getId();
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    public void method(String[] args) throws IOException {

        // Поиск файла с новостью(создавал его для теста)
        List<File> rootGoogleFolders = FindFilesByName.getGoogleFilesByName("Ивент1_текст");
        for (File file : rootGoogleFolders) {
            IventID = file.getId();
            System.out.println("Mime Type: " + file.getMimeType() + " --- Name: " + file.getName());
        }
        String fileId = IventID;

        // Получение текста из гугл документа
        OutputStream outputStream = new ByteArrayOutputStream();
        GoogleDriveUtils.getDriveService().files().export(fileId, "text/plain")
                .executeMediaAndDownloadTo(outputStream);
        System.out.println(outputStream.toString());
        // поиск папка
//        String pageToken = null;
//        do {
//            FileList result = GoogleDriveUtils.getDriveService().files().list()
//                    .setQ("mimeType='application/vnd.google-apps.folder' and name = 'Проект 1'")
//                    .setSpaces("drive")
//                    .setFields("nextPageToken, files(id, name)")
//                    .setPageToken(pageToken)
//                    .execute();
//            for (File file : result.getFiles()) {
//                System.out.printf("Found file: %s (%s)\n",
//                        file.getName(), file.getId());
//            }
//            pageToken = result.getNextPageToken();
//        } while (pageToken != null);
    }
}
