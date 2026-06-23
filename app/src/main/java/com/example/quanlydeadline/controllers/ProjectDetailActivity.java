package com.example.quanlydeadline.controllers;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.quanlydeadline.R;
import com.example.quanlydeadline.adapters.TaskAdapter;
import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.Task;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProjectDetailActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_PROJECT_NAME = "project_name";

    private TextView tvEmpty, tvProgress;
    private TaskAdapter adapter;
    private TaskDao taskDao;
    private int projectId;
    private long selectedDueDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        projectId = getIntent().getIntExtra(EXTRA_PROJECT_ID, -1);
        String projectName = getIntent().getStringExtra(EXTRA_PROJECT_NAME);

        if (projectId == -1) {
            Toast.makeText(this, "Không tìm thấy đồ án", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvProjectDetailTitle);
        tvTitle.setText(projectName != null ? projectName : "Chi tiết đồ án");

        tvEmpty = findViewById(R.id.tvEmptyTasks);
        tvProgress = findViewById(R.id.tvTaskProgress);
        RecyclerView recyclerView = findViewById(R.id.recyclerTasks);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddTask);

        taskDao = AppDatabase.getDatabase(this).taskDao();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showTaskDialog(null));

        loadTasks();
    }

    private void loadTasks() {
        List<Task> tasks = taskDao.getTasksByProject(projectId);
        adapter.setTasks(tasks);
        tvEmpty.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
        updateProgress();
    }

    private void updateProgress() {
        int total = taskDao.countAllTasks(projectId);
        int done = taskDao.countDoneTasks(projectId);
        tvProgress.setText(String.format(Locale.getDefault(), "Hoàn thành: %d/%d", done, total));
    }

    private void showTaskDialog(Task existingTask) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText edtTitle = dialogView.findViewById(R.id.edtTaskTitle);
        EditText edtNote = dialogView.findViewById(R.id.edtTaskNote);
        TextView tvPickDate = dialogView.findViewById(R.id.tvPickTaskDueDate);

        boolean isEdit = existingTask != null;
        selectedDueDate = isEdit ? existingTask.dueDate : 0;

        if (isEdit) {
            edtTitle.setText(existingTask.title);
            edtNote.setText(existingTask.note);
        }
        updatePickDateLabel(tvPickDate);

        tvPickDate.setOnClickListener(v -> showDateTimePicker(tvPickDate));

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa deadline" : "Thêm deadline mới")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (dialog, which) -> {
                    String title = edtTitle.getText().toString().trim();
                    String note = edtNote.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        existingTask.title = title;
                        existingTask.note = note;
                        existingTask.dueDate = selectedDueDate;
                        taskDao.updateTask(existingTask);
                        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                    } else {
                        Task newTask = new Task(projectId, title, note, selectedDueDate, false);
                        taskDao.insertTask(newTask);
                        Toast.makeText(this, "Đã thêm", Toast.LENGTH_SHORT).show();
                    }
                    loadTasks();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDateTimePicker(TextView tvPickDate) {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueDate > 0) {
            calendar.setTimeInMillis(selectedDueDate);
        }

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth);

            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
                picked.set(Calendar.MINUTE, minute);
                picked.set(Calendar.SECOND, 0);
                selectedDueDate = picked.getTimeInMillis();
                updatePickDateLabel(tvPickDate);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updatePickDateLabel(TextView tvPickDate) {
        if (selectedDueDate > 0) {
            tvPickDate.setText(android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", selectedDueDate));
        } else {
            tvPickDate.setText("Chọn hạn nộp (tùy chọn)");
        }
    }

    @Override
    public void onTaskCheckedChange(Task task, boolean isChecked) {
        taskDao.setTaskDone(task.id, isChecked);
        task.isDone = isChecked;
        updateProgress();
    }

    @Override
    public void onTaskEdit(Task task) {
        showTaskDialog(task);
    }

    @Override
    public void onTaskDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa deadline")
                .setMessage("Bạn có chắc muốn xóa \"" + task.title + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    taskDao.deleteTask(task);
                    loadTasks();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}