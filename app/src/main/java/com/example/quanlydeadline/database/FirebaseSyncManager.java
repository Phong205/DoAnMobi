package com.example.quanlydeadline.database;

import android.util.Log;

import com.example.quanlydeadline.models.Project;
import com.example.quanlydeadline.models.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseSyncManager {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Sync Project lên Firebase
    public void syncProject(Project project) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", project.name);
        data.put("description", project.description);
        data.put("userId", project.userId);
        data.put("dueDate", project.dueDate);

        db.collection("projects")
                .document(String.valueOf(project.id))
                .set(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firebase", "Project synced: " + project.name))
                .addOnFailureListener(e ->
                        Log.e("Firebase", "Sync failed", e));
    }

    // Sync Task lên Firebase
    public void syncTask(Task task) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", task.title);
        data.put("projectId", task.projectId);
        data.put("dueDate", task.dueDate);
        data.put("isDone", task.isDone);
        data.put("note", task.note);

        db.collection("tasks")
                .document(String.valueOf(task.id))
                .set(data);
    }

    // Lấy data từ Firebase về (khi đổi máy/login lại)
    public void fetchProjects(String userId, OnProjectsFetched callback) {
        db.collection("projects")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(callback::onFetched);
    }

    public interface OnProjectsFetched {
        void onFetched(QuerySnapshot snapshot);
    }
}
