package awesome.console.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class Notifier {

    public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("Awesome Console X");

    public static final String GROUP_ID = NOTIFICATION_GROUP.getDisplayId();

    public static void notify(Project project, @NotNull String title, @NotNull String message, @NotNull AnAction... actions) {
        notify(project, title, message, NotificationType.INFORMATION, actions);
    }

    public static void notify(Project project, @NotNull String title, @NotNull String message, NotificationType type, @NotNull AnAction... actions) {
        Notification notification = NOTIFICATION_GROUP.createNotification(GROUP_ID + ": " + title, message, type);
        notification.addActions((Collection<? extends AnAction>) List.of(actions));
        notification.setImportant(false).notify(project);
    }
}
