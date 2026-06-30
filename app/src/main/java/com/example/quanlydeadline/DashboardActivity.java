package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.adapters.TaskAdapter;
import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.NotificationHelper;
import com.example.quanlydeadline.database.NotificationSettingsDao;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.DeadlineNotification;
import com.example.quanlydeadline.models.NotificationSettings;
import com.example.quanlydeadline.models.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting, tvFilterLabel;
    private TaskDao taskDao;
    private int currentUserId;
    private RecyclerView recyclerDeadlines;
    private TaskAdapter taskAdapter;

    private TextView tvNotifBadge;
    private ImageView ivBell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        taskDao = AppDatabase.getDatabase(this).taskDao();

        txtGreeting = findViewById(R.id.txtGreeting);
        tvFilterLabel = findViewById(R.id.tvFilterLabel);
        recyclerDeadlines = findViewById(R.id.recyclerDeadlines);
        ivBell = findViewById(R.id.ivBell);
        tvNotifBadge = findViewById(R.id.tvNotifBadge);

        String fullName = getIntent().getStringExtra("FULL_NAME");
        if (fullName != null) txtGreeting.setText("Xin chào, " + fullName + " 👋");

        recyclerDeadlines.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Dùng lambda rỗng thay null — dashboard chỉ xem, không cần action
        taskAdapter = new TaskAdapter(new TaskAdapter.OnTaskActionListener() {
            @Override public void onTaskCheckedChange(Task task, boolean isChecked) {}
            @Override public void onTaskEdit(Task task) {}
            @Override public void onTaskDelete(Task task) {}
        });
        recyclerDeadlines.setAdapter(taskAdapter);

        loadAllTasks();

        // Chuông → mở NotificationActivity
        ivBell.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class))
        );

        // Filter popup
        ImageView btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, btnFilter);
            popup.getMenu().add(0, 0, 0, "Tất cả");
            popup.getMenu().add(0, 1, 1, "Sắp hết hạn");
            popup.getMenu().add(0, 2, 2, "Hoàn thành");
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == 0) { tvFilterLabel.setText("Tất cả đồ án"); loadAllTasks(); }
                else if (id == 1) { tvFilterLabel.setText("Sắp hết hạn"); loadUpcomingTasks(); }
                else if (id == 2) { tvFilterLabel.setText("Hoàn thành"); loadDoneTasks(); }
                return true;
            });
            popup.show();
        });

        findViewById(R.id.tvSeeAll).setOnClickListener(v -> navigateToProjects());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_projects) {
                startActivity(new Intent(this, ProjectListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, StatsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasksByUser(currentUserId);
            NotificationSettingsDao settingsDao = AppDatabase.getDatabase(this).notificationSettingsDao();
            NotificationSettings settings = settingsDao.getSettings(currentUserId);
            List<DeadlineNotification> notifications = NotificationHelper.generateNotifications(tasks, settings);
            int count = notifications.size();
            runOnUiThread(() -> {
                if (count > 0) {
                    tvNotifBadge.setVisibility(View.VISIBLE);
                    tvNotifBadge.setText(count > 9 ? "9+" : String.valueOf(count));
                } else {
                    tvNotifBadge.setVisibility(View.GONE);
                }
            });
        }).start();
    }

    private void loadAllTasks() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getAllTasksByUser(currentUserId);
            runOnUiThread(() -> taskAdapter.setTasks(tasks));
        }).start();
    }

    private void loadUpcomingTasks() {
        new Thread(() -> {
            long now = System.currentTimeMillis();
            long in7days = now + (7L * 24 * 60 * 60 * 1000);
            List<Task> tasks = taskDao.getUpcomingTasks(currentUserId, now, in7days);
            runOnUiThread(() -> taskAdapter.setTasks(tasks));
        }).start();
    }

    private void loadDoneTasks() {
        new Thread(() -> {
            List<Task> tasks = taskDao.getDoneTasks(currentUserId);
            runOnUiThread(() -> taskAdapter.setTasks(tasks));
        }).start();
    }

    private void navigateToProjects() {
        startActivity(new Intent(this, ProjectListActivity.class));
    }
}