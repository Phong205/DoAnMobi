package com.example.quanlydeadline.controllers;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.quanlydeadline.R;
import com.example.quanlydeadline.adapters.ProjectAdapter;
import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.ProjectDao;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.Project;

import java.util.Calendar;
import java.util.List;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ProjectListActivity extends AppCompatActivity implements ProjectAdapter.OnProjectActionListener {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProjectAdapter adapter;

    private ProjectDao projectDao;
    private TaskDao taskDao;
    private SessionManager sessionManager;
    private int currentUserId;

    private long selectedDueDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = sessionManager.getUserId();

        projectDao = AppDatabase.getDatabase(this).projectDao();
        taskDao = AppDatabase.getDatabase(this).taskDao();

        recyclerView = findViewById(R.id.recyclerProjects);
        tvEmpty = findViewById(R.id.tvEmptyProjects);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddProject);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(this);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showProjectDialog(null));

        loadProjects();
        // Bottom Navigation - highlight Projects tab
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_projects);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Quay về Dashboard
                finish();
                return true;
            } else if (id == R.id.nav_projects) {
                // Đang ở Projects, không làm gì
                return true;
            } else if (id == R.id.nav_stats) {
                return true;
            } else if (id == R.id.nav_profile) {
                // TODO: mở ProfileActivity
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        List<Project> projects = projectDao.getProjectsByUser(currentUserId);
        adapter.setProjects(projects);
        tvEmpty.setVisibility(projects.isEmpty() ? View.VISIBLE : View.GONE);
    }


    private void showProjectDialog(Project existingProject) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_project, null);
        EditText edtName = dialogView.findViewById(R.id.edtProjectName);
        EditText edtDescription = dialogView.findViewById(R.id.edtProjectDescription);
        TextView tvPickDate = dialogView.findViewById(R.id.tvPickDueDate);

        boolean isEdit = existingProject != null;
        selectedDueDate = isEdit ? existingProject.dueDate : 0;

        if (isEdit) {
            edtName.setText(existingProject.name);
            edtDescription.setText(existingProject.description);
        }
        updatePickDateLabel(tvPickDate);

        tvPickDate.setOnClickListener(v -> showDatePicker(tvPickDate));

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa đồ án" : "Thêm đồ án mới")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (dialog, which) -> {
                    String name = edtName.getText().toString().trim();
                    String description = edtDescription.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên đồ án", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        existingProject.name = name;
                        existingProject.description = description;
                        existingProject.dueDate = selectedDueDate;
                        projectDao.updateProject(existingProject);
                        Toast.makeText(this, "Đã cập nhật đồ án", Toast.LENGTH_SHORT).show();
                    } else {
                        Project newProject = new Project(
                                currentUserId,
                                name,
                                description,
                                System.currentTimeMillis(),
                                selectedDueDate
                        );
                        projectDao.insertProject(newProject);
                        Toast.makeText(this, "Đã thêm đồ án", Toast.LENGTH_SHORT).show();
                    }
                    loadProjects();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDatePicker(TextView tvPickDate) {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueDate > 0) {
            calendar.setTimeInMillis(selectedDueDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth, 23, 59, 0);
                    selectedDueDate = picked.getTimeInMillis();
                    updatePickDateLabel(tvPickDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updatePickDateLabel(TextView tvPickDate) {
        if (selectedDueDate > 0) {
            tvPickDate.setText(android.text.format.DateFormat.format("dd/MM/yyyy", selectedDueDate));
        } else {
            tvPickDate.setText("Chọn hạn chót đồ án (tùy chọn)");
        }
    }


    @Override
    public void onProjectClick(Project project) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra("project_id", project.id);
        intent.putExtra("project_name", project.name);
        startActivity(intent);
    }

    @Override
    public void onProjectEdit(Project project) {
        showProjectDialog(project);
    }

    @Override
    public void onProjectDelete(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa đồ án")
                .setMessage("Bạn có chắc muốn xóa đồ án \"" + project.name + "\"? " +
                        "Toàn bộ deadline bên trong cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    taskDao.deleteTasksByProject(project.id);
                    projectDao.deleteProject(project);
                    Toast.makeText(this, "Đã xóa đồ án", Toast.LENGTH_SHORT).show();
                    loadProjects();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
