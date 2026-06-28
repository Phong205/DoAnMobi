package com.example.quanlydeadline.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {

    // ✅ autoGenerate = true để Room tự tăng id khi insert
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

    public Project(int userId, @NonNull String name, String description, long createdAt, long dueDate) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
    }
}