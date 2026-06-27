package com.example.quanlydeadline.models;

public class DeadlineNotification {
    public Task task;
    public int type;        // 0=normal, 1=warning, 2=urgent
    public String message;
    public String timeLabel;
    public boolean isRead;

    public DeadlineNotification(Task task, int type, String message, String timeLabel) {
        this.task = task;
        this.type = type;
        this.message = message;
        this.timeLabel = timeLabel;
        this.isRead = false;
    }
}