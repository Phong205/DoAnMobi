package com.example.quanlydeadline.controllers;

import com.example.quanlydeadline.models.Project;

import java.util.ArrayList;

public class ProjectController {

    private ArrayList<Project> projectList;

    public ProjectController() {
        projectList = new ArrayList<>();
    }

    public void addProject(Project project) {
        projectList.add(project);
    }

    public void removeProject(int index) {

        if(index >= 0 &&
                index < projectList.size()) {

            projectList.remove(index);
        }
    }

    public ArrayList<Project> getProjects() {
        return projectList;
    }
}