package com.example.quanlydeadline.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.quanlydeadline.models.Project;
import com.example.quanlydeadline.models.ProjectWithProgress;

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

    @Query("SELECT * FROM projects WHERE user_id = :userId AND name LIKE '%' || :searchQuery || '%'")
    List<Project> searchProjects(int userId, String searchQuery);

    @Query("DELETE FROM projects WHERE id = :projectId")
    void deleteProjectById(int projectId);
    @Query("DELETE FROM projects WHERE user_id = :userId")
    void deleteAllByUser(int userId);

    @Query("SELECT p.*, " +
            "COUNT(t.id) as totalTasks, " +
            "SUM(CASE WHEN t.is_done = 1 THEN 1 ELSE 0 END) as doneTasks " +
            "FROM projects p " +
            "LEFT JOIN tasks t ON p.id = t.project_id " +
            "WHERE p.user_id = :userId " +
            "GROUP BY p.id " +
            "ORDER BY p.due_date ASC")
    List<ProjectWithProgress> getProjectsWithProgress(int userId);

    @Query("SELECT p.*, " +
            "COUNT(t.id) as totalTasks, " +
            "SUM(CASE WHEN t.is_done = 1 THEN 1 ELSE 0 END) as doneTasks " +
            "FROM projects p " +
            "LEFT JOIN tasks t ON p.id = t.project_id " +
            "WHERE p.user_id = :userId AND p.name LIKE '%' || :searchQuery || '%' " +
            "GROUP BY p.id " +
            "ORDER BY p.due_date ASC")
    List<ProjectWithProgress> searchProjectsWithProgress(int userId, String searchQuery);
}
