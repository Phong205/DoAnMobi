package com.example.quanlydeadline.controllers;

import com.example.quanlydeadline.models.Task;

import java.util.ArrayList;

public class TaskController {

    private ArrayList<Task> taskList;

    public TaskController() {
        taskList = new ArrayList<>();
    }

    public void addTask(Task task) {
        taskList.add(task);
    }

    public void deleteTask(int position) {

        if(position >= 0 &&
                position < taskList.size()) {

            taskList.remove(position);
        }
    }

    public ArrayList<Task> getAllTasks() {
        return taskList;
    }
}