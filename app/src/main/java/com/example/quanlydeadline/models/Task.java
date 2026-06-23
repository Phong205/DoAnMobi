package com.example.quanlydeadline.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String projectName;
    private String deadline;
    private String priority;
    private int progress;

    public Task(
            String title,
            String projectName,
            String deadline,
            String priority,
            int progress) {

        this.title = title;
        this.projectName = projectName;
        this.deadline = deadline;
        this.priority = priority;
        this.progress = progress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getPriority() {
        return priority;
    }

    public int getProgress() {
        return progress;
    }
}