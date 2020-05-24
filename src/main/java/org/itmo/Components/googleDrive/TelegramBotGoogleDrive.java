package org.itmo.Components.googleDrive;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Component
public class TelegramBotGoogleDrive {
    // Параметры
  //  @Value("${projectName}")
    public String PROJECT_NAME_FOLDER = "Проект 1";

    public final String NEW_NAME_FILE = "дз1";

   // @Value("${HWDirectory}")
    public String HOMEWORK_DIRECTORY = "Домашнее задание";

    public  String IventID;

    File folder_hw;
    File folder_student;



    public TelegramBotGoogleDrive(){

        deploy();
    }

    //-------------------------------------ЗАПУСК ОДИН РАЗ СРАЗУ ПОСЛЕ ДЕПЛОЯ -------------------------
    public void deploy() {
        try {
            // Создание папки проекта
            File folder_project = CreateFolder.createGoogleFolder(null, PROJECT_NAME_FOLDER);
            // Создание папки с домашними заданиями всех пользователей
            folder_hw = CreateFolder.createGoogleFolder(folder_project.getId(), HOMEWORK_DIRECTORY);
            System.out.println(folder_hw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------ЗАПУСК ОДИН РАЗ СРАЗУ ПОСЛЕ АКТИВАЦИИ БОТА -----------------
    public File activate(String username) {
        // Создание папки для студента
        //find user_sheet_name == true

        //else
        try {
            folder_student = CreateFolder.createGoogleFolder(folder_hw.getId(), username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folder_student;
    }

    // -----------------ЗАПУСК КАЖДЫЙ РАЗ ПОСЛЕ АКТИВАЦИИ КНОПКИ "Отправить дз -> Дз1" -----------------
    public String sendHomework(InputStream inputStream, String fileName, File folder_student) {
        System.out.println("sendHomework");
        // Создание файлов с дз
        File googleFile = null;
        try {
            ////System.getProperty("user.home")+
            googleFile = CreateHWFile.createGoogleFile(folder_student.getId(), CheckTypeDoc.CheckType(fileName), NEW_NAME_FILE, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Отправить пользователю
        return "Ссылка для просмотра: " + googleFile.getWebViewLink();

    }
    // Поиск папки студента
    public boolean findDirectory(String userNameSheet){
        try {
            List<File> rootGoogleParentFolders = GetSubFoldersByName.getGoogleRootFoldersByName(PROJECT_NAME_FOLDER);
            for (File folder : rootGoogleParentFolders) {
                GetSubFoldersByName.FOLDER_PARENT_ID = folder.getId();
            }

            List<File> rootGoogleFolders = GetSubFoldersByName.getGoogleSubFolderByName(GetSubFoldersByName.FOLDER_PARENT_ID, NEW_NAME_FILE);
            for (File folder : rootGoogleFolders) {
                System.out.println("Folder ID: " + folder.getId() + " --- Name: " + folder.getName());
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
