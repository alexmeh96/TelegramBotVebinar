package org.itmo.Components.googleDrive;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

/**
 * Класс для поиска папок на Google Диск
 */
public class GetSubFoldersByName {
    /**
     * Поле родительской папки
     */
    public static String FOLDER_PARENT_ID;

    /**
     * Класс для поиска папок на Google Диск по id родителя
     * @param googleFolderIdParent id родительской папки
     * @param subFolderName имя папки
     * @return лист с найденными папками
     * @throws IOException
     */
    public static final List<File> getGoogleSubFolderByName(String googleFolderIdParent, String subFolderName)
            throws IOException {

        Drive driveService = GoogleDriveUtils.getDriveService();

        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String query = null;
        if (googleFolderIdParent == null) {
            query = " name = '" + subFolderName + "' " //
                    + " and mimeType = 'application/vnd.google-apps.folder' " //
                    + " and 'root' in parents";
        } else {
            query = " name = '" + subFolderName + "' " //
                    + " and mimeType = 'application/vnd.google-apps.folder' " //
                    + " and '" + googleFolderIdParent + "' in parents";
        }

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive") //
                    .setFields("nextPageToken, files(id, name, createdTime)")//
                    .setPageToken(pageToken).execute();
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        //
        return list;
    }

    /**
     * Класс для поиска папок на Google Диск без id родителя
     * @param subFolderName имя папки
     * @return лист с найденными папками
     * @throws IOException
     */
    public static final List<File> getGoogleRootFoldersByName(String subFolderName) throws IOException {
        return getGoogleSubFolderByName(null,subFolderName);
    }



}