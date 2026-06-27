package com.example.quanlydeadline.database;

import com.example.quanlydeadline.models.Task;
import com.example.quanlydeadline.models.DeadlineNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Logic tạo thông báo theo 3 trạng thái:
 * - 1-4 ngày còn lại  → 🔴 KHẨN: "Nước tới đầu rồi, nhanh nộp!"
 * - 5-9 ngày còn lại  → 🟡 GẤP: "Deadline sắp đến, cần chú ý!"
 * - 10 ngày còn lại   → 🔵 BÌNH THƯỜNG: "Deadline đang đến gần"
 */
public class NotificationHelper {

    public static final int TYPE_URGENT = 2;   // ≤ 1 ngày
    public static final int TYPE_WARNING = 1;  // 2-5 ngày
    public static final int TYPE_NORMAL = 0;   // 6-10 ngày

    public static List<DeadlineNotification> generateNotifications(List<Task> tasks) {
        List<DeadlineNotification> notifications = new ArrayList<>();
        long now = System.currentTimeMillis();
        long day = 24L * 60 * 60 * 1000;

        for (Task task : tasks) {
            if (task.isDone || task.dueDate <= 0) continue;

            long diff = task.dueDate - now;
            long daysLeft = diff / day;

            if (daysLeft < 0) {
                // Đã quá hạn
                notifications.add(new DeadlineNotification(
                        task,
                        TYPE_URGENT,
                        "⛔ Task '" + task.title + "' đã quá hạn " + Math.abs(daysLeft) + " ngày!",
                        Math.abs(daysLeft) + " ngày trước"
                ));
            } else if (daysLeft == 0) {
                // Hôm nay
                notifications.add(new DeadlineNotification(
                        task,
                        TYPE_URGENT,
                        "🚨 Nước tới đầu rồi! '" + task.title + "' hết hạn HÔM NAY!",
                        "Hôm nay"
                ));
            } else if (daysLeft == 1) {
                notifications.add(new DeadlineNotification(
                        task,
                        TYPE_URGENT,
                        "🔴 Còn 1 ngày! Nhanh nộp '" + task.title + "'!",
                        "Còn 1 ngày"
                ));
            } else if (daysLeft <= 5) {
                notifications.add(new DeadlineNotification(
                        task,
                        TYPE_WARNING,
                        "⚠️ Deadline gấp! '" + task.title + "' còn " + daysLeft + " ngày",
                        "Còn " + daysLeft + " ngày"
                ));
            } else if (daysLeft <= 10) {
                notifications.add(new DeadlineNotification(
                        task,
                        TYPE_NORMAL,
                        "📅 '" + task.title + "' deadline còn " + daysLeft + " ngày",
                        "Còn " + daysLeft + " ngày"
                ));
            }
            // Trên 10 ngày: không tạo thông báo
        }

        // Sắp xếp: urgent trước, rồi warning, rồi normal
        notifications.sort((a, b) -> b.type - a.type);

        return notifications;
    }
}