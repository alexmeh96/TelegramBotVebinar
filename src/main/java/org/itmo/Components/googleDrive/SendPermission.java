package org.itmo.Components.googleDrive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;

public class SendPermission {
    //    константа с почтой спикера(проверяющего)
    public static String GOOGLE_SPEAKER_EMAIL = "alex.meh96@gmail.com";
    public static String GOOGLE_STUDENT_EMAIL = "alex.meh96@gmail.com";


    // Даём доступ
    public static Permission createPermissionForEmailSPEAKER(String googleFileId, String googleEmail) throws IOException {
        // Все значения: пользователи - группы - домен - никому
        String permissionType = "user"; // Допустимо: пользователь, группа (user, group, domain, anyone)
        // организатор-владелец-писатель-комментатор-читатель
        String permissionRole = "writer"; // Допустимо: organizer - owner - writer - commenter - reader

        //настраиваем доступ
        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        newPermission.setEmailAddress(googleEmail);

        Drive driveService = GoogleDriveUtils.getDriveService();
        return driveService.permissions().create(googleFileId, newPermission).execute();
    }

    public static Permission createPermissionForEmailSTUDENT(String googleFileId, String googleEmail) throws IOException {
        // Все значения: пользователи - группы - домен - никому
        String permissionType = "user"; // Допустимо: пользователь, группа (user, group, domain, anyone)
        // организатор-владелец-писатель-комментатор-читатель
        String permissionRole = "reader"; // Допустимо: organizer - owner - writer - commenter - reader

        //настраиваем доступ
        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);

        newPermission.setEmailAddress(googleEmail);

        Drive driveService = GoogleDriveUtils.getDriveService();
        return driveService.permissions().create(googleFileId, newPermission).execute();
    }
}
