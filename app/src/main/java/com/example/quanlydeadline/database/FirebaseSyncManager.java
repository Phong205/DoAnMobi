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

    // ===================== PROJECT =====================

    // ✅ Sync Project lên Firebase — luôn gửi kèm updatedAt
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

    // ✅ Sync Task lên Firebase (đã thêm projectId đầy đủ)
    public void syncTask(Task task) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", task.title);
        data.put("projectId", task.projectId);
        data.put("dueDate", task.dueDate);
        data.put("isDone", task.isDone);
        data.put("note", task.note);
        data.put("priority", task.priority);
        data.put("fileName", task.fileName);
        data.put("fileUrl", task.fileUrl);

        db.collection("tasks")
                .document(String.valueOf(task.id))
                .set(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firebase", "Task synced: " + task.title))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Task sync failed", e));
    }

    // ✅ Fetch projects từ Firestore về Room khi mở app
    // ✅ Fetch projects từ Firestore về Room — CHỈ ghi đè nếu bản server mới hơn bản local
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
                                    // Chưa có ở Room -> insert mới, lấy nguyên dữ liệu từ server
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
                                    // ✅ ĐIỂM MẤU CHỐT: bản trên server MỚI HƠN bản ở máy
                                    // -> mới được phép ghi đè. Nếu bản local mới hơn (người dùng
                                    // vừa sửa offline) thì KHÔNG ghi đè, để tránh mất dữ liệu mới.
                                    existing.name = doc.getString("name") != null ? doc.getString("name") : existing.name;
                                    existing.description = doc.getString("description") != null ? doc.getString("description") : existing.description;
                                    existing.dueDate = doc.getLong("dueDate") != null ? doc.getLong("dueDate") : existing.dueDate;
                                    existing.updatedAt = remoteUpdatedAt;
                                    projectDao.updateProject(existing);
                                }
                                // else: bản local mới hơn hoặc bằng -> bỏ qua, giữ nguyên local.
                                // (Bản local mới hơn sẽ được đẩy lên server ở lượt syncProject() kế tiếp.)

                            } catch (NumberFormatException e) {
                                Log.e("Firebase", "Invalid doc ID", e);
                            }
                        }
                        if (onComplete != null) onComplete.run();
                    }).start();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Fetch failed", e));
    }

    // ===================== TASK =====================

    // ✅ Sync Task lên Firebase — luôn gửi kèm updatedAt
    public void syncTask(Task task) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", task.title);
        data.put("projectId", task.projectId);
        data.put("dueDate", task.dueDate);
        data.put("isDone", task.isDone);
        data.put("note", task.note);
        data.put("priority", task.priority);
        data.put("updatedAt", task.updatedAt); // ✅ MỚI

        db.collection("tasks")
                .document(String.valueOf(task.id))
                .set(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firebase", "Task synced: " + task.title))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Task sync failed", e));
    }

    // ✅ Fetch tasks từ Firestore về Room — cùng nguyên tắc "mới hơn mới được ghi đè"
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
                                    task.priority = doc.getLong("priority") != null
                                            ? doc.getLong("priority").intValue()
                                            : 1;

                                    task.fileName = doc.getString("fileName");
                                    task.fileUrl = doc.getString("fileUrl");
                                    task.id = docId;
                                    task.updatedAt = remoteUpdatedAt;
                                    if (doc.getLong("priority") != null) {
                                        task.priority = doc.getLong("priority").intValue();
                                    }
                                    taskDao.insertTask(task);
                                    Log.d("Firebase", "Fetched task: " + task.title);
                                } else {
                                    existing.title = doc.getString("title");
                                    existing.note = doc.getString("note");
                                    existing.dueDate = doc.getLong("dueDate") != null
                                            ? doc.getLong("dueDate")
                                            : 0;

                                    existing.isDone = Boolean.TRUE.equals(doc.getBoolean("isDone"));

                                    existing.priority = doc.getLong("priority") != null
                                            ? doc.getLong("priority").intValue()
                                            : 1;

                                    existing.fileName = doc.getString("fileName");
                                    existing.fileUrl = doc.getString("fileUrl");

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

    public void deleteTask(int taskId) {
        db.collection("tasks")
                .document(String.valueOf(taskId))
                .delete()
                .addOnSuccessListener(unused -> Log.d("Firebase", "Task deleted: " + taskId))
                .addOnFailureListener(e -> Log.e("Firebase", "Delete task failed", e));
    }

}
