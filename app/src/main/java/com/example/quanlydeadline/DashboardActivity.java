package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.adapters.TaskAdapter;
import com.example.quanlydeadline.controllers.ProjectListActivity;
import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting;
    private TextView tvFilterLabel;
    private TaskDao taskDao;
    private int currentUserId;
    private RecyclerView recyclerDeadlines;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Khởi tạo DAO và session
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        taskDao = AppDatabase.getDatabase(this).taskDao();

        txtGreeting = findViewById(R.id.txtGreeting);
        tvFilterLabel = findViewById(R.id.tvFilterLabel);
        recyclerDeadlines = findViewById(R.id.recyclerDeadlines);

        String fullName = getIntent().getStringExtra("FULL_NAME");
        if (fullName != null) {
            txtGreeting.setText("Xin chào, " + fullName + " 👋");
        }

        // Setup RecyclerView
        recyclerDeadlines.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(null);
        recyclerDeadlines.setAdapter(taskAdapter);

        // Load mặc định: tất cả
        loadAllTasks();

        // Bộ lọc
        ImageView btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, btnFilter);
            popup.getMenu().add(0, 0, 0, "Tổng đồ án");
            popup.getMenu().add(0, 1, 1, "Sắp hết hạn");
            popup.getMenu().add(0, 2, 2, "Hoàn thành");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        tvFilterLabel.setText("Tất cả đồ án");
                        loadAllTasks();
                        break;
                    case 1:
                        tvFilterLabel.setText("Sắp hết hạn");
                        loadUpcomingTasks();
                        break;
                    case 2:
                        tvFilterLabel.setText("Hoàn thành");
                        loadDoneTasks();
                        break;
                }
                return true;
            });
            popup.show();
        });

        // Xem tất cả
        findViewById(R.id.tvSeeAll).setOnClickListener(v -> navigateToProjects());

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_projects) { navigateToProjects(); return true; }
            else if (id == R.id.nav_stats) return true;
            else if (id == R.id.nav_profile) return true;
            return false;
        });
    }

    private void loadAllTasks() {
        List<Task> tasks = taskDao.getAllTasksByUser(currentUserId);
        taskAdapter.setTasks(tasks);
    }

    private void loadUpcomingTasks() {
        long now = System.currentTimeMillis();
        long in7days = now + (7L * 24 * 60 * 60 * 1000);
        List<Task> tasks = taskDao.getUpcomingTasks(currentUserId, now, in7days);
        taskAdapter.setTasks(tasks);
    }

    private void loadDoneTasks() {
        List<Task> tasks = taskDao.getDoneTasks(currentUserId);
        taskAdapter.setTasks(tasks);
    }

    private void navigateToProjects() {
        startActivity(new Intent(this, ProjectListActivity.class));
    }
}