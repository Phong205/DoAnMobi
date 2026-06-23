package com.example.quanlydeadline.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.quanlydeadline.models.Project;

@Dao
public interface ProjectDao {

    @Insert
    long insertProject(Project project);

    @Update
    void updateProject(Project project);

    @Delete
    void deleteProject(Project project);

    @Query("SELECT * FROM projects WHERE user_id = :userId ORDER BY due_date ASC")
    List<Project> getProjectsByUser(int userId);

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    Project getProjectById(int projectId);

    @Query("DELETE FROM projects WHERE id = :projectId")
    void deleteProjectById(int projectId);
}
