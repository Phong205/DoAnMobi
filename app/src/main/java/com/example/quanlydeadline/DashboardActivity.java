package com.example.quanlydeadline;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.NotificationHelper;
import com.example.quanlydeadline.database.NotificationSettingsDao;
import com.example.quanlydeadline.database.ProjectDao;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.DeadlineNotification;
import com.example.quanlydeadline.models.NotificationSettings;
import com.example.quanlydeadline.models.Project;
import com.example.quanlydeadline.models.ProjectWithProgress;
import com.example.quanlydeadline.models.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtGreeting, tvFilterLabel;
    private ProjectDao projectDao;
    private TaskDao taskDao;
    private int currentUserId;
    private RecyclerView recyclerDeadlines;
    private ProjectCardAdapter projectCardAdapter;

    private TextView tvNotifBadge;
    private ImageView ivBell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        projectDao = AppDatabase.getDatabase(this).projectDao();
        taskDao = AppDatabase.getDatabase(this).taskDao();

        txtGreeting = findViewById(R.id.txtGreeting);
        tvFilterLabel = findViewById(R.id.tvFilterLabel);
        recyclerDeadlines = findViewById(R.id.recyclerDeadlines);
        ivBell = findViewById(R.id.ivBell);
        tvNotifBadge = findViewById(R.id.tvNotifBadge);

        String fullName = sessionManager.getFullName();
        if (fullName != null && !fullName.isEmpty()) {
            txtGreeting.setText("Xin chào, " + fullName + " 👋");
        } else {
            txtGreeting.setText("Xin chào 👋");
        }

        recyclerDeadlines.setLayoutManager(new LinearLayoutManager(this));
        projectCardAdapter = new ProjectCardAdapter();
        recyclerDeadlines.setAdapter(projectCardAdapter);

        loadAllProjects();

        ivBell.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class))
        );

        ImageView btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, btnFilter);
            popup.getMenu().add(0, 0, 0, "Tất cả");
            popup.getMenu().add(0, 1, 1, "Sắp hết hạn");
            popup.getMenu().add(0, 2, 2, "Hoàn thành");
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == 0) { tvFilterLabel.setText("Tất cả đồ án"); loadAllProjects(); }
                else if (id == 1) { tvFilterLabel.setText("Sắp hết hạn"); loadUpcomingProjects(); }
                else if (id == 2) { tvFilterLabel.setText("Hoàn thành"); loadDoneProjects(); }
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

    // ===== Load danh sách Project cho card =====

    private void loadAllProjects() {
        new Thread(() -> {
            List<ProjectWithProgress> projects = projectDao.getProjectsWithProgress(currentUserId);
            List<CardData> cards = buildCardData(projects);
            runOnUiThread(() -> projectCardAdapter.setItems(cards));
        }).start();
    }

    private void loadUpcomingProjects() {
        new Thread(() -> {
            long now = System.currentTimeMillis();
            long in7days = now + (7L * 24 * 60 * 60 * 1000);
            List<ProjectWithProgress> all = projectDao.getProjectsWithProgress(currentUserId);
            List<ProjectWithProgress> filtered = new ArrayList<>();
            for (ProjectWithProgress p : all) {
                if (p.project.dueDate > 0 && p.project.dueDate >= now && p.project.dueDate <= in7days) {
                    filtered.add(p);
                }
            }
            List<CardData> cards = buildCardData(filtered);
            runOnUiThread(() -> projectCardAdapter.setItems(cards));
        }).start();
    }

    private void loadDoneProjects() {
        new Thread(() -> {
            List<ProjectWithProgress> all = projectDao.getProjectsWithProgress(currentUserId);
            List<ProjectWithProgress> filtered = new ArrayList<>();
            for (ProjectWithProgress p : all) {
                if (p.totalTasks > 0 && p.doneTasks == p.totalTasks) {
                    filtered.add(p);
                }
            }
            List<CardData> cards = buildCardData(filtered);
            runOnUiThread(() -> projectCardAdapter.setItems(cards));
        }).start();
    }

    private List<CardData> buildCardData(List<ProjectWithProgress> projects) {
        List<CardData> cards = new ArrayList<>();
        for (ProjectWithProgress p : projects) {
            Project project = p.project;
            int maxPriority = taskDao.getMaxPriorityByProject(project.id);
            CardData card = new CardData();
            card.name = project.name;
            card.tag = project.description;
            card.dueDate = project.dueDate;
            card.totalTasks = p.totalTasks;
            card.doneTasks = p.doneTasks;
            card.priority = maxPriority;
            cards.add(card);
        }
        return cards;
    }

    private void navigateToProjects() {
        startActivity(new Intent(this, ProjectListActivity.class));
    }

    // ===== Lớp dữ liệu nội bộ (không tạo file riêng) =====

    private static class CardData {
        String name;
        String tag;
        long dueDate;
        int totalTasks;
        int doneTasks;
        int priority;

        int getPercent() {
            if (totalTasks == 0) return 0;
            return (int) ((doneTasks * 100.0) / totalTasks);
        }
    }

    // ===== Adapter nội bộ (không tạo file riêng) =====

    private class ProjectCardAdapter extends RecyclerView.Adapter<ProjectCardAdapter.VH> {

        private List<CardData> items = new ArrayList<>();

        void setItems(List<CardData> newItems) {
            this.items = newItems != null ? newItems : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dashboard_project, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            CardData item = items.get(position);

            holder.tvProjectName.setText(item.name);

            if (item.tag != null && !item.tag.trim().isEmpty()) {
                holder.tvProjectTag.setVisibility(View.VISIBLE);
                holder.tvProjectTag.setText(item.tag);
            } else {
                holder.tvProjectTag.setVisibility(View.GONE);
            }

            if (item.dueDate > 0) {
                holder.tvDueDate.setText(
                        android.text.format.DateFormat.format("dd/MM/yyyy", item.dueDate));
            } else {
                holder.tvDueDate.setText("Chưa đặt hạn");
            }

            int percent = item.getPercent();
            holder.tvProgressPercent.setText(percent + "%");
            holder.progressBar.setProgress(percent);

            String label;
            int bgColor, textColor;
            switch (item.priority) {
                case 2:
                    label = "● Cao"; bgColor = Color.parseColor("#FEE2E2"); textColor = Color.parseColor("#EF4444");
                    break;
                case 0:
                    label = "● Thấp"; bgColor = Color.parseColor("#DCFCE7"); textColor = Color.parseColor("#22C55E");
                    break;
                default:
                    label = "● Trung bình"; bgColor = Color.parseColor("#FEF3C7"); textColor = Color.parseColor("#F59E0B");
            }
            holder.tvPriorityBadge.setText(label);
            holder.tvPriorityBadge.setTextColor(textColor);
            holder.tvPriorityBadge.getBackground().mutate().setTint(bgColor);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvProjectName, tvProjectTag, tvDueDate, tvProgressPercent, tvPriorityBadge;
            ProgressBar progressBar;

            VH(@NonNull View itemView) {
                super(itemView);
                tvProjectName     = itemView.findViewById(R.id.tvProjectName);
                tvProjectTag      = itemView.findViewById(R.id.tvProjectTag);
                tvDueDate         = itemView.findViewById(R.id.tvDueDate);
                tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
                tvPriorityBadge   = itemView.findViewById(R.id.tvPriorityBadge);
                progressBar       = itemView.findViewById(R.id.progressBar);
            }
        }
    }
}