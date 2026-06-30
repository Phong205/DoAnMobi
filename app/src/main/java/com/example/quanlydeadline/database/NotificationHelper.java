package com.example.quanlydeadline.database;

import com.example.quanlydeadline.models.NotificationSettings;
import com.example.quanlydeadline.models.Task;
import com.example.quanlydeadline.models.DeadlineNotification;

import java.util.ArrayList;
import java.util.List;


public class NotificationHelper {

    public static final int TYPE_URGENT = 2;
    public static final int TYPE_WARNING = 1;
    public static final int TYPE_NORMAL = 0;

    public static List<DeadlineNotification> generateNotifications(List<Task> tasks) {
        return generateNotifications(tasks, null);
    }

    public static List<DeadlineNotification> generateNotifications(List<Task> tasks, NotificationSettings settings) {
        List<DeadlineNotification> notifications = new ArrayList<>();

        if (settings != null && (!settings.enableAll)) {
            return notifications;
        }

        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;

        for (Task task : tasks) {
            if (task.isDone || task.dueDate <= 0) continue;

            long diff = task.dueDate - now;
            long daysLeft = diff / day;

            if (daysLeft < 0) {
                if (settings == null || settings.remindOverdue) {
                    notifications.add(new DeadlineNotification(
                            task, TYPE_URGENT,
                            "⛔ Task '" + task.title + "' đã quá hạn " + Math.abs(daysLeft) + " ngày!",
                            Math.abs(daysLeft) + " ngày trước"
                    ));
                }
            } else if (daysLeft == 0) {
                if (settings == null || settings.remind1Day) {
                    notifications.add(new DeadlineNotification(
                            task, TYPE_URGENT,
                            "🚨 Nước tới đầu rồi! '" + task.title + "' hết hạn HÔM NAY!",
                            "Hôm nay"
                    ));
                }
            } else if (daysLeft == 1) {
                if (settings == null || settings.remind1Day) {
                    notifications.add(new DeadlineNotification(
                            task, TYPE_URGENT,
                            "🔴 Còn 1 ngày! Nhanh nộp '" + task.title + "'!",
                            "Còn 1 ngày"
                    ));
                }
            } else if (daysLeft <= 5) {
                if (settings == null || settings.remind5Days) {
                    notifications.add(new DeadlineNotification(
                            task, TYPE_WARNING,
                            "⚠️ Deadline gấp! '" + task.title + "' còn " + daysLeft + " ngày",
                            "Còn " + daysLeft + " ngày"
                    ));
                }
            } else if (daysLeft <= 10) {
                if (settings == null || settings.remind10Days) {
                    notifications.add(new DeadlineNotification(
                            task, TYPE_NORMAL,
                            "📅 '" + task.title + "' deadline còn " + daysLeft + " ngày",
                            "Còn " + daysLeft + " ngày"
                    ));
                }
            }
        }

        notifications.sort((a, b) -> b.type - a.type);
        return notifications;
    }
}