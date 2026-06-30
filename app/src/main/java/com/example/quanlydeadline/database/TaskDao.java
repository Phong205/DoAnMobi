package com.example.quanlydeadline.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.quanlydeadline.models.Task;

@Dao
public interface TaskDao {

    @Insert
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks WHERE project_id = :projectId ORDER BY due_date ASC")
    List<Task> getTasksByProject(int projectId);

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task getTaskById(int taskId);

    @Query("UPDATE tasks SET is_done = :isDone WHERE id = :taskId")
    void setTaskDone(int taskId, boolean isDone);

    @Query("SELECT COUNT(*) FROM tasks WHERE project_id = :projectId AND is_done = 1")
    int countDoneTasks(int projectId);

    @Query("SELECT COUNT(*) FROM tasks WHERE project_id = :projectId")
    int countAllTasks(int projectId);

    @Query("DELETE FROM tasks WHERE project_id = :projectId")
    void deleteTasksByProject(int projectId);

    @Query("SELECT t.* FROM tasks t INNER JOIN projects p ON t.project_id = p.id WHERE p.user_id = :userId ORDER BY t.due_date ASC")
    List<Task> getAllTasksByUser(int userId);

    @Query("SELECT t.* FROM tasks t INNER JOIN projects p ON t.project_id = p.id WHERE p.user_id = :userId AND t.is_done = 0 AND t.due_date BETWEEN :now AND :in7days ORDER BY t.due_date ASC")
    List<Task> getUpcomingTasks(int userId, long now, long in7days);

    @Query("SELECT t.* FROM tasks t INNER JOIN projects p ON t.project_id = p.id WHERE p.user_id = :userId AND t.is_done = 1 ORDER BY t.due_date ASC")
    List<Task> getDoneTasks(int userId);

    // ✅ MỚI
    @Query("UPDATE tasks SET updated_at = :updatedAt WHERE id = :taskId")
    void touchUpdatedAt(int taskId, long updatedAt);

    @Query("SELECT MAX(priority) FROM tasks WHERE project_id = :projectId AND is_done = 0")
    int getMaxPriorityByProject(int projectId);
}
