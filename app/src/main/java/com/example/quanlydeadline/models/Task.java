package com.example.quanlydeadline.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Task = một deadline / công việc con bên trong một Project (đồ án).
 * Ví dụ đồ án "Web bán hàng" có thể có nhiều Task: "Nộp đề cương", "Demo lần 1", "Nộp báo cáo cuối"...
 */
@Entity(
        tableName = "tasks",
        foreignKeys = @ForeignKey(
                entity = Project.class,
                parentColumns = "id",
                childColumns = "project_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("project_id")}
)
public class Task {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "project_id")
    public int projectId;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "due_date")
    public long dueDate; // millis (System.currentTimeMillis())

    @ColumnInfo(name = "is_done")
    public boolean isDone;

    public Task(int projectId, @NonNull String title, String note, long dueDate, boolean isDone) {
        this.projectId = projectId;
        this.title = title;
        this.note = note;
        this.dueDate = dueDate;
        this.isDone = isDone;
    }
}