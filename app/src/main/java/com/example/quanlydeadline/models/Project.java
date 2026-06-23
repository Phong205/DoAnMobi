package com.example.quanlydeadline.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Đồ án (Project) thuộc về một User.
 * Mỗi đồ án có thể chứa nhiều Task (deadline / công việc con).
 */
@Entity(
        tableName = "projects",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("user_id")}
)
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
    public long dueDate; // Hạn chót tổng của đồ án (mốc nộp cuối cùng), millis. 0 nếu chưa đặt.

    public Project(int userId, @NonNull String name, String description, long createdAt, long dueDate) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
    }
}