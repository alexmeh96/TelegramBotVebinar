package org.itmo.Components.googleDrive;

import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Component
public class TelegramBotGoogleDrive {
    // Параметры
  //  @Value("${projectName}")
    public String PROJECT_NAME_FOLDER = "Проект 1";

    public final String NEW_NAME_FILE = "дз1";

    public final String HW = "HW";

   // @Value("${HWDirectory}")
    public String HOMEWORK_DIRECTORY = "Домашнее задание";

    public static String MainID, HWID, HW_day, fileid;

    private File folder_hw;
    private File folder_student;
    private File folder_project;
    private File folderHW;
    private Map<String, File> fileMapHW = new HashMap<>();;

    public TelegramBotGoogleDrive(){
        //deploy();
        folder_project = findFolder(PROJECT_NAME_FOLDER, null);
        folder_hw = findFolder(HOMEWORK_DIRECTORY, folder_project);
        folderHW = findFolder(HW, folder_project);

        fileMapHW.put("HW_1", findFolder("HW_1",folderHW));
//        fileMapHW.put("HW_2", findFolder("HW_2",folderHW));
//        fileMapHW.put("HW_3", findFolder("HW_3",folderHW));

    }

    public Map<String, File> getFileMapHW() {
        return fileMapHW;
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
        try {
            folder_student = CreateFolder.createGoogleFolder(folder_hw.getId(), username);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folder_student;
    }

    // -----------------ЗАПУСК КАЖДЫЙ РАЗ ПОСЛЕ АКТИВАЦИИ КНОПКИ "Отправить дз -> Дз1" -----------------
    public String sendHomework(InputStream inputStream, String fileName, String newFileName, File folder_student) {
        System.out.println("sendHomework");
        // Создание файлов с дз
        File googleFile = null;
        try {
            ////System.getProperty("user.home")+
            googleFile = CreateHWFile.createGoogleFile(folder_student.getId(), CheckTypeDoc.CheckType(fileName), newFileName, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Отправить пользователю
        return "Ссылка для просмотра: " + googleFile.getWebViewLink();

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
            e.printStackTrace();
        }
        return null;
    }

    public void method2(File file){
        String fileId = file.getId();
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            GoogleDriveUtils.getDriveService().files().export(fileId, CheckTypeDoc.CheckType(file.getName()))
                    .executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
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

    public String getFileId(String ParentsID) throws IOException {
        String fileNameLike = "HW_video";
        String type = "video/mp4";
        Drive driveService = GoogleDriveUtils.getDriveService();

        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String query = " name contains '" + fileNameLike + "' " +
                " '" + ParentsID + "' in parents " //
                + " and mimeType != '"+ type + "' ";

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    // Fields will be assigned values: id, name, createdTime, mimeType
                    .setFields("nextPageToken, files(id, name, createdTime, mimeType)")//
                    .setPageToken(pageToken).execute();
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return list.get(0).getId();
    }




    public InputStream downloadFile(String nameFolder) throws IOException {

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

        List<File> hw_file_GoogleFolders = FindFilesByName.getGoogleFilesByName("HW_text");
        List<File> hw_video_GoogleFolders = FindFilesByName.getGoogleFilesByName("HW_video");

        File file = hw_file_GoogleFolders.get(0);
        File video = hw_video_GoogleFolders.get(0);

        System.out.println(video);


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
