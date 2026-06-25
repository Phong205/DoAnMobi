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

    // ✅ Sync Project lên Firebase (đã thêm createdAt)
    public void syncProject(Project project) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", project.name);
        data.put("description", project.description);
        data.put("userId", project.userId);
        data.put("dueDate", project.dueDate);
        data.put("createdAt", project.createdAt); // ✅ thêm field này

        db.collection("projects")
                .document(String.valueOf(project.id))
                .set(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firebase", "Project synced: " + project.name))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Sync failed", e));
    }

    // ✅ Sync Task lên Firebase (đã thêm projectId đầy đủ)
    public void syncTask(Task task) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", task.title);
        data.put("projectId", task.projectId);
        data.put("dueDate", task.dueDate);
        data.put("isDone", task.isDone);
        data.put("note", task.note);

        db.collection("tasks")
                .document(String.valueOf(task.id))
                .set(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firebase", "Task synced: " + task.title))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Task sync failed", e));
    }

    // ✅ Fetch projects từ Firestore về Room khi mở app
    public void fetchAndSaveProjects(int userId, ProjectDao projectDao, Runnable onComplete) {
        db.collection("projects")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    new Thread(() -> {
                        // ✅ Xóa hết data cũ trong Room trước, rồi insert lại từ Firestore
                        projectDao.deleteAllByUser(userId);

                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            try {
                                int docId = Integer.parseInt(doc.getId());
                                Project project = new Project(
                                        userId,
                                        doc.getString("name") != null ? doc.getString("name") : "",
                                        doc.getString("description") != null ? doc.getString("description") : "",
                                        doc.getLong("createdAt") != null ? doc.getLong("createdAt") : 0,
                                        doc.getLong("dueDate") != null ? doc.getLong("dueDate") : 0
                                );
                                project.id = docId;
                                projectDao.insertProject(project);
                            } catch (NumberFormatException e) {
                                Log.e("Firebase", "Invalid doc ID", e);
                            }
                        }
                        if (onComplete != null) onComplete.run();
                    }).start();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Fetch failed", e));
    }

    // ✅ Fetch tasks từ Firestore về Room
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

                                if (existing == null) {
                                    Task task = new Task(
                                            projectId,
                                            doc.getString("title") != null ? doc.getString("title") : "",
                                            doc.getString("note") != null ? doc.getString("note") : "",
                                            doc.getLong("dueDate") != null ? doc.getLong("dueDate") : 0,
                                            Boolean.TRUE.equals(doc.getBoolean("isDone"))
                                    );
                                    task.id = docId;
                                    taskDao.insertTask(task);
                                    Log.d("Firebase", "Fetched task: " + task.title);
                                }
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
}