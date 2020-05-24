package org.itmo.Components.googleDrive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.itmo.Components.googleSheet.BotGoogleSheet;

import java.io.IOException;

public class SendPermission {
    public static final Logger LOGGER = LoggerFactory.getLogger(SendPermission.class);

    //    константа с почтой спикера(проверяющего)
    @Value("${SpeakerMail}")
    private String SpeakerMail;
    BotGoogleSheet sheet = new BotGoogleSheet();
    public String GOOGLE_SPEAKER_EMAIL = SpeakerMail;
    public static String GOOGLE_STUDENT_EMAIL = "mark.gurianov@gmail.com";


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
        LOGGER.info("Спикеру предоставлен доступ к {}", googleFileId);
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
        LOGGER.info("Студенту предоставлен доступ к {}", googleFileId);
        return driveService.permissions().create(googleFileId, newPermission).execute();
    }
}
