package org.itmo.Components.googleDrive;

import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.extern.slf4j.Slf4j;
import org.itmo.Components.googleSheet.BotGoogleSheet;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.*;

@SuppressWarnings("ALL")
@Slf4j
@Component
public class TelegramBotGoogleDrive {

    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(TelegramBotGoogleDrive.class);

    public static String MainID, HWID, HW_day, fileid;

    private File studentsFolderHW;
    private File studentFolder;
    private File projectFolder;
    private File folderHW;
    private Map<String, File> fileMapHW = new HashMap<>();;

    public TelegramBotGoogleDrive(){
        //deploy();
        projectFolder = findFolder("Проект 1", null);
        studentsFolderHW = findFolder("Домашнее задание", projectFolder);
        folderHW = findFolder("HW", projectFolder);

        fileMapHW.put("HW_1", findFolder("HW_1", folderHW));
        fileMapHW.put("HW_2", findFolder("HW_2",folderHW));
        fileMapHW.put("HW_3", findFolder("HW_3",folderHW));

    }

    public Map<String, File> getFileMapHW() {
        return fileMapHW;
    }

    //-------------------------------------ЗАПУСК ОДИН РАЗ СРАЗУ ПОСЛЕ ДЕПЛОЯ -------------------------
    public void deploy() {
        try {
            // Создание папки проекта
            File folder_project = CreateFolder.createGoogleFolder(null, "Проект 1");
            log.info("Создана папка проекта: {}", folder_project);

            // Создание папки с домашними заданиями всех пользователей
            studentsFolderHW = CreateFolder.createGoogleFolder(folder_project.getId(), "Домашнее задание");
            log.info("Создана папка для домашних заданий: {}", studentsFolderHW);
//            System.out.println(studentsFolderHW);
        } catch (IOException e) {
            log.trace("Ошибка создания основных папок в Google Drive: {}", e.getStackTrace());
//            e.printStackTrace();
        }
        try {
            BotGoogleSheet.DeployUpdateAll();
            log.info("Данные из листа формы записаны в основной лист таблицы");
        } catch (IOException e) {
            log.trace("Ошибка записи данных в основной лист: {]", e.getStackTrace());
//            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            log.trace("Ошибка записи данных в основной лист: {]", e.getStackTrace());
//            e.printStackTrace();
        }
    }

    // -------------------------------------ЗАПУСК ОДИН РАЗ СРАЗУ ПОСЛЕ АКТИВАЦИИ БОТА -----------------
    public File activate(String username) {
        try {
            studentFolder = CreateFolder.createGoogleFolder(studentsFolderHW.getId(), username);
            log.info("Создана папка для дз студента {} : {}", username, studentsFolderHW);
        } catch (IOException e) {
            log.trace("Ошибка создания папки с дз студента {} : {}", username, e.getStackTrace());
//            e.printStackTrace();
        }
        return studentFolder;
    }

    // -----------------ЗАПУСК КАЖДЫЙ РАЗ ПОСЛЕ АКТИВАЦИИ КНОПКИ "Отправить дз -> Дз1" -----------------
    public boolean sendHomework(InputStream inputStream, String fileName, String newFileName, File folder_student) {
        log.info("Отправка домашнего задани {}",folder_student.getName());
//        System.out.println("sendHomework");
        // Создание файлов с дз
        File googleFile = null;
        try {
            ////System.getProperty("user.home")+
            log.info("В папку студента загружен файл с дз");
            googleFile = CreateHWFile.createGoogleFile(folder_student.getId(), CheckTypeDoc.CheckType(fileName), newFileName, inputStream);
            return true;
        } catch (IOException e) {
            log.trace("Ошибка загрузки файла с дз: {}", e.getStackTrace());
//            e.printStackTrace();
        }
        // Отправить пользователю
        return false;

    }


    public File findFolder(String fileName, File folderSearch){
        try {
            List<File> fileList;
            if(folderSearch == null)
                fileList = GetSubFoldersByName.getGoogleRootFoldersByName(fileName);
            else
                fileList = GetSubFoldersByName.getGoogleSubFolderByName(folderSearch.getId(), fileName);
            return fileList.get(0);
        } catch (IOException e) {
            log.trace("Ошибка поиска папки {} : {}", fileName, e.getStackTrace());
//            e.printStackTrace();
        }
        return null;
    }

    public void method2(File file){
        String fileId = file.getId();
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            log.info("Успешный экспорт {}  в OutputStream", file.getName());
            GoogleDriveUtils.getDriveService().files().export(fileId, CheckTypeDoc.CheckType(file.getName()))
                    .executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            log.trace("Ошибка экспорта {}  в OutputStream: {}", file.getName(), e.getStackTrace());
//            e.printStackTrace();
        }

        try {
            Drive.Files f = GoogleDriveUtils.getDriveService().files();
            HttpResponse httpResponse = null;
            String fname = file.getName();
            String ex = fname.substring(fname.lastIndexOf(".") + 1);

//            if (ex.equalsIgnoreCase("xlsx")) {
//                httpResponse = f
//                        .export(file.getId(),
//                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
//                        .executeMedia();
//
//            } else if (ex.equalsIgnoreCase("docx")) {
//                httpResponse = f
//                        .export(file.getId(),
//                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
//                        .executeMedia();
//            } else if (ex.equalsIgnoreCase("pptx")) {
//                httpResponse = f
//                        .export(file.getId(),
//                                "application/vnd.openxmlformats-officedocument.presentationml.presentation")
//                        .executeMedia();
//
//            } else if (ex.equalsIgnoreCase("pdf")
//                    || ex.equalsIgnoreCase("jpg")
//                    || ex.equalsIgnoreCase("png")) {
//
//                Drive.Files.Get get = f.get(file.getId());
//                httpResponse = get.executeMedia();
//
//            }
            if (null != httpResponse) {
                InputStream instream = httpResponse.getContent();
//                FileOutputStream output = new FileOutputStream(
//                        file.getName());
//                try {
//                    int l;
//                    byte[] tmp = new byte[2048];
//                    while ((l = instream.read(tmp)) != -1) {
//                        output.write(tmp, 0, l);
//                    }
//                } finally {
//                    output.close();
//                    instream.close();
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileVideoId(String ParentsID){
        String fileNameLike = "HW_video.mp4";
        String type = "video/mp4";
        Drive driveService = null;
        try {
            driveService = GoogleDriveUtils.getDriveService();
        } catch (IOException e) {
            log.trace("Ошибка подключения driveService : {}", e.getStackTrace());
        }

        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String query = "name = '" + fileNameLike + "' and '" + ParentsID + "' in parents " + " and mimeType = '"+ type + "' ";
      //  String query = "name = '" + fileNameLike + "'";



        do {
            FileList result = null;//!!!!!!
            try {
                result = driveService.files().list()
                        .setQ(query)
                        .setSpaces("drive") //
                        // Fields will be assigned values: id, name, createdTime, mimeType
                        .setFields("nextPageToken, files(id, name, createdTime, mimeType)")//
                        .setPageToken(pageToken).execute();
            } catch (IOException e) {
                log.trace("Не найден {} : {}", fileNameLike, e.getStackTrace());
//                e.printStackTrace();
            }
            System.out.println(result);
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return list.get(0).getId();
    }

    public String getTextHW(String ParentsID){

        String fileNameLike = "HW_text";
        String type = "application/vnd.google-apps.document";
        Drive driveService = null;
        try {
            driveService = GoogleDriveUtils.getDriveService();
        } catch (IOException e) {
            log.trace("Ошибка подключения driveService : {}", e.getStackTrace());
        }

        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String query = "name = '" + fileNameLike + "' and '" + ParentsID + "' in parents " + " and mimeType = '"+ type + "' ";
        //String query = "name = '" + fileNameLike + "'";

        do {
            FileList result = null;//!!!!!!
            try {
                result = driveService.files().list()
                        .setQ(query)
                        .setSpaces("drive") //
                        // Fields will be assigned values: id, name, createdTime, mimeType
                        .setFields("nextPageToken, files(id, name, createdTime, mimeType)")//
                        .setPageToken(pageToken).execute();
            } catch (IOException e) {
                log.trace("Не найден {} : {}", fileNameLike, e.getStackTrace());
//                e.printStackTrace();
            }
            System.out.println(result);
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            GoogleDriveUtils.getDriveService().files().export(list.get(0).getId(), "text/plain")
                    .executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            log.trace("Ошибка получения текста дз для рассылки : {}", e.getStackTrace());
//            e.printStackTrace();
        }
        log.info("Текст сообщения из рассылки дз : \n {}", outputStream.toString());
//        System.out.println(outputStream.toString());


        return outputStream.toString();
    }




    public InputStream downloadFile(String nameFolder) {

        // Поиск файла с новостью(создавал его для теста)
//        List<File> mainGoogleFolders = GetSubFoldersByName.getGoogleRootFoldersByName("Проект 1");
//        for (File folder : mainGoogleFolders) {
//            MainID = folder.getId();
//        }
//
//
//        List<File> hwGoogleFolders = GetSubFoldersByName.getGoogleSubFolderByName(MainID, "HW");
//        for (File folder : hwGoogleFolders) {
//            HWID = folder.getId();
//        }
//
//        List<File> hw_day_GoogleFolders = GetSubFoldersByName.getGoogleSubFolderByName(HWID, "HW_" + "1");
//        for (File folder : hw_day_GoogleFolders) {
//            HW_day = folder.getId();
//        }

        List<File> hw_file_GoogleFolders = null;
        try {
            hw_file_GoogleFolders = FindFilesByName.getGoogleFilesByName("HW_text");
            log.info("Получен текст сообщения из рассылки дз");
        } catch (IOException e) {
            log.info("Ошибка получения текста сообщения из рассылки дз : {}", e.getStackTrace());
//            e.printStackTrace();
        }
        List<File> hw_video_GoogleFolders = null;
        try {
            hw_video_GoogleFolders = FindFilesByName.getGoogleFilesByName("HW_video");
            log.info("Получено видео сообщения из рассылки дз");
        } catch (IOException e) {
            log.info("Ошибка получения видео сообщения из рассылки дз : {}", e.getStackTrace());
//            e.printStackTrace();
        }

        File file = hw_file_GoogleFolders.get(0);
        File video = hw_video_GoogleFolders.get(0);

        try {
            GoogleDriveUtils.getDriveService().files()
                                            .export(file.getId(), "text/plain");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // video/mp4
//        HttpResponse httpResponse = GoogleDriveUtils.getDriveService().files()
//                                        .export(file.getId(), "text/plain")
//                                        .executeMedia();
        InputStream inputStream = null;
//        if (null != httpResponse)
//            inputStream = httpResponse.getContent();
//
        return inputStream;



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
