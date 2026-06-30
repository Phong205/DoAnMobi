package com.example.quanlydeadline.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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
    public long dueDate;

    @ColumnInfo(name = "is_done")
    public boolean isDone;

    @ColumnInfo(name = "priority")
    public int priority; // 0=thấp, 1=trung bình, 2=cao

    // ✅ Thêm field fileName và fileUrl
    @ColumnInfo(name = "file_name")
    public String fileName;

    @ColumnInfo(name = "file_url")
    public String fileUrl;
    // ✅ MỚI: thời điểm task này được sửa lần cuối (local hoặc từ server)
    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public Task(int projectId, @NonNull String title, String note, long dueDate, boolean isDone) {
        this.projectId = projectId;
        this.title = title;
        this.note = note;
        this.dueDate = dueDate;
        this.isDone = isDone;
        this.priority = 1;// mặc định trung bình
        this.fileName = null;
        this.fileUrl = null;
    }
    }
        this.updatedAt = System.currentTimeMillis();
    }
}
