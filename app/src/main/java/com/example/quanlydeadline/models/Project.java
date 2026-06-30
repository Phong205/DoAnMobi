package com.example.quanlydeadline.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "due_date")
    public long dueDate;

    // ✅ MỚI: thời điểm bản ghi này được sửa lần cuối (local hoặc từ server).
    // Dùng để so sánh khi đồng bộ Room <-> Firestore: bản nào có updatedAt lớn hơn thì thắng.
    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public Project(int userId, @NonNull String name, String description, long createdAt, long dueDate) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.updatedAt = createdAt; // mặc định lúc tạo mới, updatedAt = createdAt
    }
}
