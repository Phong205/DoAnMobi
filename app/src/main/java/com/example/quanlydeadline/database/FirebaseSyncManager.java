package com.example.quanlydeadline.database;

import android.util.Log;

import com.example.quanlydeadline.models.Project;
import com.example.quanlydeadline.models.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseSyncManager {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public void syncProject(Project project) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", project.name);
        data.put("description", project.description);
        data.put("userId", project.userId);
        data.put("dueDate", project.dueDate);
        data.put("createdAt", project.createdAt);
        data.put("updatedAt", project.updatedAt); // ✅ MỚI

        db.collection("projects")
                .document(String.valueOf(project.id))
                .set(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firebase", "Project synced: " + project.name))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Sync failed", e));
    }

    public void fetchAndSaveProjects(int userId, ProjectDao projectDao, Runnable onComplete) {
        db.collection("projects")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    new Thread(() -> {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            try {
                                int docId = Integer.parseInt(doc.getId());
                                Project existing = projectDao.getProjectById(docId);

                                long remoteUpdatedAt = doc.getLong("updatedAt") != null
                                        ? doc.getLong("updatedAt") : 0;

                                if (existing == null) {
                                    Project project = new Project(
                                            userId,
                                            doc.getString("name") != null ? doc.getString("name") : "",
                                            doc.getString("description") != null ? doc.getString("description") : "",
                                            doc.getLong("createdAt") != null ? doc.getLong("createdAt") : 0,
                                            doc.getLong("dueDate") != null ? doc.getLong("dueDate") : 0
                                    );
                                    project.id = docId;
                                    project.updatedAt = remoteUpdatedAt;
                                    projectDao.insertProject(project);

                                } else if (remoteUpdatedAt > existing.updatedAt) {
                                    existing.name = doc.getString("name") != null ? doc.getString("name") : existing.name;
                                    existing.description = doc.getString("description") != null ? doc.getString("description") : existing.description;
                                    existing.dueDate = doc.getLong("dueDate") != null ? doc.getLong("dueDate") : existing.dueDate;
                                    existing.updatedAt = remoteUpdatedAt;
                                    projectDao.updateProject(existing);
                                }

                            } catch (NumberFormatException e) {
                                Log.e("Firebase", "Invalid doc ID", e);
                            }
                        }
                        if (onComplete != null) onComplete.run();
                    }).start();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Fetch failed", e));
    }


    public void syncTask(Task task) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", task.title);
        data.put("projectId", task.projectId);
        data.put("dueDate", task.dueDate);
        data.put("isDone", task.isDone);
        data.put("note", task.note);
        data.put("priority", task.priority);
        data.put("updatedAt", task.updatedAt); // ✅ MỚI
        data.put("fileUrl", task.fileUrl);   // ✅ MỚI: đồng bộ file đính kèm
        data.put("fileName", task.fileName); // ✅ MỚI

        db.collection("tasks")
                .document(String.valueOf(task.id))
                .set(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firebase", "Task synced: " + task.title))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Task sync failed", e));
    }

    public void fetchAndSaveTasks(int projectId, TaskDao taskDao, Runnable onComplete) {
        db.collection("tasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    new Thread(() -> {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            try {
                                int docId = Integer.parseInt(doc.getId());
                                Task existing = taskDao.getTaskById(docId);

                                long remoteUpdatedAt = doc.getLong("updatedAt") != null
                                        ? doc.getLong("updatedAt") : 0;

                                if (existing == null) {
                                    Task task = new Task(
                                            projectId,
                                            doc.getString("title") != null ? doc.getString("title") : "",
                                            doc.getString("note") != null ? doc.getString("note") : "",
                                            doc.getLong("dueDate") != null ? doc.getLong("dueDate") : 0,
                                            Boolean.TRUE.equals(doc.getBoolean("isDone"))
                                    );
                                    task.id = docId;
                                    task.updatedAt = remoteUpdatedAt;
                                    if (doc.getLong("priority") != null) {
                                        task.priority = doc.getLong("priority").intValue();
                                    }
                                    task.fileUrl = doc.getString("fileUrl");   // ✅ MỚI
                                    task.fileName = doc.getString("fileName"); // ✅ MỚI
                                    taskDao.insertTask(task);
                                    Log.d("Firebase", "Fetched task: " + task.title);

                                } else if (remoteUpdatedAt > existing.updatedAt) {
                                    // ✅ Chỉ ghi đè khi server mới hơn
                                    existing.title = doc.getString("title") != null ? doc.getString("title") : existing.title;
                                    existing.note = doc.getString("note") != null ? doc.getString("note") : existing.note;
                                    existing.dueDate = doc.getLong("dueDate") != null ? doc.getLong("dueDate") : existing.dueDate;
                                    existing.isDone = Boolean.TRUE.equals(doc.getBoolean("isDone"));
                                    if (doc.getLong("priority") != null) {
                                        existing.priority = doc.getLong("priority").intValue();
                                    }
                                    existing.fileUrl = doc.getString("fileUrl");   // ✅ MỚI
                                    existing.fileName = doc.getString("fileName"); // ✅ MỚI
                                    existing.updatedAt = remoteUpdatedAt;
                                    taskDao.updateTask(existing);
                                }
                                // else: local mới hơn -> giữ nguyên, không ghi đè.

                            } catch (NumberFormatException e) {
                                Log.e("Firebase", "Invalid task doc ID", e);
                            }
                        }
                        if (onComplete != null) onComplete.run();
                    }).start();
                })
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Fetch tasks failed", e)
                );
    }

    public interface OnProjectsFetched {
        void onFetched(QuerySnapshot snapshot);
    }

    public void deleteProject(int projectId) {
        db.collection("projects")
                .document(String.valueOf(projectId))
                .delete()
                .addOnSuccessListener(unused -> Log.d("Firebase", "Project deleted: " + projectId))
                .addOnFailureListener(e -> Log.e("Firebase", "Delete project failed", e));
    }

    public void deleteTasksByProject(int projectId) {
        db.collection("tasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete()
                                .addOnSuccessListener(unused ->
                                        Log.d("Firebase", "Orphan task deleted: " + doc.getId()))
                                .addOnFailureListener(e ->
                                        Log.e("Firebase", "Delete orphan task failed", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Fetch tasks to delete failed", e));
    }

    public void deleteTask(int taskId) {
        db.collection("tasks")
                .document(String.valueOf(taskId))
                .delete()
                .addOnSuccessListener(unused -> Log.d("Firebase", "Task deleted: " + taskId))
                .addOnFailureListener(e -> Log.e("Firebase", "Delete task failed", e));
    }

}