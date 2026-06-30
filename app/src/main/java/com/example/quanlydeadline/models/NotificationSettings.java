package com.example.quanlydeadline.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Bảng cài đặt thông báo — mỗi user có đúng 1 dòng (id = userId).
 * Lưu trong Room, không cần Firebase vì chỉ ảnh hưởng UX trên máy hiện tại.
 */
@Entity(tableName = "notification_settings")
public class NotificationSettings {

    @PrimaryKey
    public int userId; // dùng userId làm key, mỗi user 1 dòng duy nhất

    @ColumnInfo(name = "enable_all")
    public boolean enableAll = true;

    @ColumnInfo(name = "sound")
    public boolean sound = true;

    @ColumnInfo(name = "vibrate")
    public boolean vibrate = false;

    @ColumnInfo(name = "remind_10_days")
    public boolean remind10Days = true;

    @ColumnInfo(name = "remind_5_days")
    public boolean remind5Days = true;

    @ColumnInfo(name = "remind_1_day")
    public boolean remind1Day = true;

    @ColumnInfo(name = "remind_overdue")
    public boolean remindOverdue = true;

    public NotificationSettings() {
        // Cần constructor rỗng cho Room
    }

    public NotificationSettings(int userId) {
        this.userId = userId;
    }
}