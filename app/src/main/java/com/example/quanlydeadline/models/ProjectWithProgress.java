package com.example.quanlydeadline.models;

import androidx.room.Embedded;

public class ProjectWithProgress {
    @Embedded
    public Project project;

    public int totalTasks;
    public int doneTasks;
    public ProjectWithProgress() {
    }
    public ProjectWithProgress(Project project, int totalTasks, int doneTasks) {
        this.project = project;
        this.totalTasks = totalTasks;
        this.doneTasks = doneTasks;
    }
    // Hàm tự tính % tiến độ
    public int getProgressPercentage() {
        if (totalTasks == 0) return 0;
        return (doneTasks * 100) / totalTasks;
    }
}
