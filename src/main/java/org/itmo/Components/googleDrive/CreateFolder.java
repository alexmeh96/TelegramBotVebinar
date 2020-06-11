package org.itmo.Components.googleDrive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CreateFolder {

    // Создаём папку
    public static final File createGoogleFolder(String folderIdParent, String folderName) throws IOException {

        File fileMetadata = new File();




        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        if (folderIdParent != null) {
            List<String> parents = Arrays.asList(folderIdParent);

            fileMetadata.setParents(parents);///////
        }
        Drive driveService = GoogleDriveUtils.getDriveService();

        // Создание Folder.
        // Возвращает File object с id и именем полей будут присвоены значения
        File file = driveService.files().create(fileMetadata).setFields("id, name").execute();


        // Даём спикеру доступ к дз
        //SendPermission.createPermissionForEmailSPEAKER(file.getId(), SendPermission.GOOGLE_SPEAKER_EMAIL);

        // Даём студенту доступ к дз
       // SendPermission.createPermissionForEmailSTUDENT(file.getId(), SendPermission.GOOGLE_STUDENT_EMAIL);

        return file;
    }

}
