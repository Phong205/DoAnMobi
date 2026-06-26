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
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.LoginActivity;
import com.example.quanlydeadline.models.ProjectWithProgress;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.quanlydeadline.R;
import com.example.quanlydeadline.adapters.ProjectAdapter;
import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.ProjectDao;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.Project;
import com.example.quanlydeadline.database.FirebaseSyncManager;

import java.util.ArrayList;
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
    private FirebaseSyncManager syncManager;

    private boolean hasFetched = false;

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
        SearchView searchView = findViewById(R.id.searchProject);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(this);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showProjectDialog(null));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        loadProjects();
        syncManager = new FirebaseSyncManager();


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

        // ✅ Chỉ fetch từ Firestore 1 lần duy nhất khi mở app
        if (!hasFetched) {
            hasFetched = true;
            syncManager.fetchAndSaveProjects(currentUserId, projectDao, () -> {
                runOnUiThread(() -> loadProjects());
            });
        }
    }

    private void loadProjects() {
        List<ProjectWithProgress> list = projectDao.getProjectsWithProgress(currentUserId);
        adapter.setProjects(list);

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            loadProjects();
        } else {
            // 1. Lấy danh sách Project thô từ database theo từ khóa tìm kiếm
            List<Project> rawProjects = projectDao.searchProjects(currentUserId, query);

            // 2. Tạo một danh sách mới chứa kiểu dữ liệu mà Adapter yêu cầu
            List<ProjectWithProgress> projectsWithProgress = new ArrayList<>();

            // 3. Vòng lặp chuyển đổi từng Project thành ProjectWithProgress
            for (Project p : rawProjects) {
                // Lấy số lượng công việc của đồ án này để tính tiến độ
                int total = taskDao.countAllTasks(p.id);
                int done = taskDao.countDoneTasks(p.id);

                // Khởi tạo Object chứa tiến độ (Thay đổi constructor tùy theo cấu trúc class ProjectWithProgress của bạn)
                ProjectWithProgress pwp = new ProjectWithProgress(p, total, done);

                projectsWithProgress.add(pwp);
            }

            // 4. Truyền danh sách đã chuyển đổi vào Adapter (Hết lỗi gạch đỏ)
            adapter.setProjects(projectsWithProgress);
            updateUI(rawProjects); // Hoặc updateUI(projectsWithProgress) tùy hàm updateUI của bạn nhận gì
        }
    }

    private void updateUI(List<Project> projects) {
        if (projects.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            // Kiểm tra SearchView có đang nhập gì không
            SearchView searchView = findViewById(R.id.searchProject);
            String query = searchView.getQuery().toString();
            if (!query.isEmpty()) {
                tvEmpty.setText("Không tìm thấy đồ án liên quan");
            } else {
                tvEmpty.setText("Chưa có đồ án nào");
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        TextView tvCount = findViewById(R.id.tvProjectCount);
        if (tvCount != null) {
            tvCount.setText(projects.size() + " đồ án");
        }
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
                        syncManager.syncProject(existingProject);
                        Toast.makeText(this, "Đã cập nhật đồ án", Toast.LENGTH_SHORT).show();
                    } else {
                        Project newProject = new Project(
                                currentUserId,
                                name,
                                description,
                                System.currentTimeMillis(),
                                selectedDueDate
                        );
                        long newId = projectDao.insertProject(newProject);
                        newProject.id = (int) newId;
                        syncManager.syncProject(newProject);
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
