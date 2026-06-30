package com.example.quanlydeadline;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlydeadline.adapters.TaskAdapter;
import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.FirebaseSyncManager;
import com.example.quanlydeadline.database.TaskDao;
import com.example.quanlydeadline.models.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.widget.SeekBar;
import android.widget.RelativeLayout;

import com.google.android.material.textfield.TextInputEditText;

public class ProjectDetailActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {
    private Uri selectedFileUri = null;
    private String selectedFileName = null;
    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_PROJECT_NAME = "project_name";

    private TextView tvEmpty, tvProgress, tvPercent;
    private ProgressBar progressOverall;
    private TaskAdapter adapter;
    private TaskDao taskDao;
    private FirebaseSyncManager syncManager;
    private int projectId;
    private long selectedDueDate = 0;
    private TextView currentTvFileName = null;
    private android.widget.ImageButton currentBtnClearFile = null;
    // Tab filter: "todo", "inprogress", "done", "overdue"
    private String currentTab = "todo";

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

        // Header
        TextView tvTitle = findViewById(R.id.tvProjectDetailTitle);
        tvTitle.setText(projectName != null ? projectName : "Chi tiết đồ án");

        // Nút back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Views
        tvEmpty = findViewById(R.id.tvEmptyTasks);
        tvProgress = findViewById(R.id.tvTaskProgress);
        tvPercent = findViewById(R.id.tvProjectPercent);
        progressOverall = findViewById(R.id.progressOverall);

        RecyclerView recyclerView = findViewById(R.id.recyclerTasks);

        // ✅ Đúng kiểu — ExtendedFloatingActionButton
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAddTask);

        taskDao = AppDatabase.getDatabase(this).taskDao();
        syncManager = new FirebaseSyncManager();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> showTaskDialog(null));

        // Setup tabs
        setupTabs();

        // Fetch tasks từ Firestore về Room
        syncManager.fetchAndSaveTasks(projectId, taskDao, () ->
                runOnUiThread(() -> loadTasks())
        );

        loadTasks();
    }

    private void setupTabs() {
        TextView tabTodo = findViewById(R.id.tabTodo);
        TextView tabInProgress = findViewById(R.id.tabInProgress);
        TextView tabDone = findViewById(R.id.tabDone);
        TextView tabOverdue = findViewById(R.id.tabOverdue);

        tabTodo.setOnClickListener(v -> {
            currentTab = "todo";
            updateTabUI(tabTodo, tabInProgress, tabDone, tabOverdue);
            loadTasks();
        });
        tabInProgress.setOnClickListener(v -> {
            currentTab = "inprogress";
            updateTabUI(tabInProgress, tabTodo, tabDone, tabOverdue);
            loadTasks();
        });
        tabDone.setOnClickListener(v -> {
            currentTab = "done";
            updateTabUI(tabDone, tabTodo, tabInProgress, tabOverdue);
            loadTasks();
        });
        tabOverdue.setOnClickListener(v -> {
            currentTab = "overdue";
            updateTabUI(tabOverdue, tabTodo, tabInProgress, tabDone);
            loadTasks();
        });
    }

    private void updateTabUI(TextView active, TextView... inactives) {
        active.setBackgroundResource(R.drawable.tab_selected_bg);
        active.setTextColor(0xFFFFFFFF);
        active.setTypeface(null, android.graphics.Typeface.BOLD);
        for (TextView tab : inactives) {
            tab.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            tab.setTextColor(0xFFC7D9FF);
            tab.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void loadTasks() {
        List<Task> allTasks = taskDao.getTasksByProject(projectId);
        List<Task> filtered = filterTasks(allTasks);
        adapter.setTasks(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        updateProgress(allTasks);
    }

    private List<Task> filterTasks(List<Task> all) {
        List<Task> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Task t : all) {
            switch (currentTab) {
                case "todo":
                    if (!t.isDone && (t.dueDate == 0 || t.dueDate > now)) result.add(t);
                    break;
                case "inprogress":
                    if (!t.isDone) result.add(t);
                    break;
                case "done":
                    if (t.isDone) result.add(t);
                    break;
                case "overdue":
                    if (!t.isDone && t.dueDate > 0 && t.dueDate < now) result.add(t);
                    break;
            }
        }
        return result;
    }

    private void updateProgress(List<Task> allTasks) {
        int total = allTasks.size();
        int done = 0;
        for (Task t : allTasks) if (t.isDone) done++;

        tvProgress.setText(String.format(Locale.getDefault(), "%d/%d tasks", done, total));

        int percent = total > 0 ? (done * 100 / total) : 0;
        tvPercent.setText(percent + "%");
        progressOverall.setProgress(percent);
    }

// Thay toàn bộ hàm showTaskDialog() trong ProjectDetailActivity.java bằng đoạn này:

    private void showTaskDialog(Task existingTask) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);

        // Views
        TextInputEditText edtTitle = dialogView.findViewById(R.id.edtTaskTitle);
        TextInputEditText edtNote = dialogView.findViewById(R.id.edtTaskNote);
        RelativeLayout layoutPickDate = dialogView.findViewById(R.id.tvPickTaskDueDate); // ✅ RelativeLayout
        TextView tvDeadlineDate = dialogView.findViewById(R.id.tvDeadlineDate);
        TextView tvDeadlineTime = dialogView.findViewById(R.id.tvDeadlineTime);
        TextView tvProgressPercent = dialogView.findViewById(R.id.tvProgressPercent);
        SeekBar seekBarProgress = dialogView.findViewById(R.id.seekBarProgress);
        TextView btnLow = dialogView.findViewById(R.id.btnPriorityLow);
        TextView btnMedium = dialogView.findViewById(R.id.btnPriorityMedium);
        TextView btnHigh = dialogView.findViewById(R.id.btnPriorityHigh);

        // ── THÊM VÀO SAU DÒNG: TextView btnHigh = dialogView.findViewById(R.id.btnPriorityHigh);

        com.google.android.material.button.MaterialButton btnAttachFile = dialogView.findViewById(R.id.btnAttachFile);
        TextView tvAttachedFileName = dialogView.findViewById(R.id.tvAttachedFileName);
        android.widget.ImageButton btnClearFile = dialogView.findViewById(R.id.btnClearFile);

// Reset file mỗi lần mở dialog
        selectedFileUri = null;
        selectedFileName = null;
        tvAttachedFileName.setText("Chưa có file nào");
        btnClearFile.setVisibility(View.GONE);

// Click "Chọn File" → mở file picker
        btnAttachFile.setOnClickListener(v -> {
            // Lưu tạm 2 view này để dùng trong callback filePickerLauncher
            currentTvFileName = tvAttachedFileName;
            currentBtnClearFile = btnClearFile;
            openFilePicker();
        });

// Click X → xóa file đã chọn
        btnClearFile.setOnClickListener(v -> {
            selectedFileUri = null;
            selectedFileName = null;
            tvAttachedFileName.setText("Chưa có file nào");
            btnClearFile.setVisibility(View.GONE);
        });

        boolean isEdit = existingTask != null;
        selectedDueDate = isEdit ? existingTask.dueDate : 0;
        final int[] selectedPriority = {1}; // 0=thấp, 1=trung bình, 2=cao

        if (isEdit) {
            edtTitle.setText(existingTask.title);
            edtNote.setText(existingTask.note);

            if (existingTask.fileName != null && !existingTask.fileName.isEmpty()) {
                tvAttachedFileName.setText(existingTask.fileName);
                btnClearFile.setVisibility(View.VISIBLE);
            }
        }
        // Cập nhật label deadline
        updateDeadlineLabel(tvDeadlineDate, tvDeadlineTime);

        // Click chọn ngày giờ deadline
        layoutPickDate.setOnClickListener(v ->
                showDateTimePicker(tvDeadlineDate, tvDeadlineTime)
        );

        // SeekBar tiến độ
        seekBarProgress.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                tvProgressPercent.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
            }
        });

        // Priority buttons
        btnLow.setOnClickListener(v -> {
            selectedPriority[0] = 0;
            btnLow.setBackgroundResource(R.drawable.priority_selected_low);
            btnLow.setTextColor(0xFFFFFFFF);
            btnMedium.setBackgroundResource(R.drawable.priority_unselected_medium);
            btnMedium.setTextColor(0xFFF59E0B);
            btnHigh.setBackgroundResource(R.drawable.priority_unselected_high);
            btnHigh.setTextColor(0xFFEF4444);
        });
        btnMedium.setOnClickListener(v -> {
            selectedPriority[0] = 1;
            btnLow.setBackgroundResource(R.drawable.priority_unselected_low);
            btnLow.setTextColor(0xFF16A34A);
            btnMedium.setBackgroundResource(R.drawable.priority_selected_medium);
            btnMedium.setTextColor(0xFFFFFFFF);
            btnHigh.setBackgroundResource(R.drawable.priority_unselected_high);
            btnHigh.setTextColor(0xFFEF4444);
        });
        btnHigh.setOnClickListener(v -> {
            selectedPriority[0] = 2;
            btnLow.setBackgroundResource(R.drawable.priority_unselected_low);
            btnLow.setTextColor(0xFF16A34A);
            btnMedium.setBackgroundResource(R.drawable.priority_unselected_medium);
            btnMedium.setTextColor(0xFFF59E0B);
            btnHigh.setBackgroundResource(R.drawable.priority_selected_high);
            btnHigh.setTextColor(0xFFFFFFFF);
        });

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Sửa deadline" : "Thêm deadline mới")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (dialog, which) -> {
                    String title = edtTitle.getText().toString().trim();
                    String note = edtNote.getText() != null ? edtNote.getText().toString().trim() : "";

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        existingTask.title = title;
                        existingTask.note = note;
                        existingTask.dueDate = selectedDueDate;
                        existingTask.priority = selectedPriority[0];

                        if (existingTask.fileUrl != null) {
                            File oldFile = new File(existingTask.fileUrl);
                            if (oldFile.exists()) {
                                oldFile.delete();
                            }
                        }
                        // Nếu có file mới đính kèm khi edit
                        if (selectedFileUri != null) {
                            existingTask.fileName = selectedFileName;
                            try {
                                java.io.File attachDir = new java.io.File(getFilesDir(), "attachments");
                                if (!attachDir.exists()) attachDir.mkdirs();
                                String fName = System.currentTimeMillis() + "_" + selectedFileName;
                                java.io.File destFile = new java.io.File(attachDir, fName);
                                java.io.InputStream in = getContentResolver().openInputStream(selectedFileUri);
                                java.io.FileOutputStream out = new java.io.FileOutputStream(destFile);
                                byte[] buffer = new byte[4096];
                                int len;
                                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                                in.close();
                                out.close();
                                existingTask.fileUrl = destFile.getAbsolutePath();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        taskDao.updateTask(existingTask);
                        syncManager.syncTask(existingTask);
                        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                        loadTasks();

                    } else {
                        Task newTask = new Task(projectId, title, note, selectedDueDate, false);
                        newTask.priority = selectedPriority[0];

                        // Lưu file local nếu có
                        if (selectedFileUri != null) {
                            try {
                                java.io.File attachDir = new java.io.File(getFilesDir(), "attachments");
                                if (!attachDir.exists()) attachDir.mkdirs();
                                String fName = System.currentTimeMillis() + "_" + selectedFileName;
                                java.io.File destFile = new java.io.File(attachDir, fName);
                                java.io.InputStream in = getContentResolver().openInputStream(selectedFileUri);
                                java.io.FileOutputStream out = new java.io.FileOutputStream(destFile);
                                byte[] buffer = new byte[4096];
                                int len;
                                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                                in.close();
                                out.close();
                                newTask.fileUrl = destFile.getAbsolutePath();
                                newTask.fileName = selectedFileName;
                                Toast.makeText(this, "✅ Đã đính kèm: " + selectedFileName, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(this, "⚠️ Lưu file thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        long newId = taskDao.insertTask(newTask);
                        newTask.id = (int) newId;
                        syncManager.syncTask(newTask);
                        Toast.makeText(this, "Đã thêm task", Toast.LENGTH_SHORT).show();
                        loadTasks();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

// Thay hàm showDateTimePicker và updatePickDateLabel bằng:

    private void showDateTimePicker(TextView tvDate, TextView tvTime) {
        Calendar calendar = Calendar.getInstance();
        if (selectedDueDate > 0) calendar.setTimeInMillis(selectedDueDate);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hourOfDay);
                picked.set(Calendar.MINUTE, minute);
                picked.set(Calendar.SECOND, 0);
                selectedDueDate = picked.getTimeInMillis();
                updateDeadlineLabel(tvDate, tvTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDeadlineLabel(TextView tvDate, TextView tvTime) {
        if (selectedDueDate > 0) {
            tvDate.setText(android.text.format.DateFormat.format("dd/MM/yyyy", selectedDueDate));
            tvTime.setText(android.text.format.DateFormat.format("HH:mm", selectedDueDate));
        } else {
            tvDate.setText("Chọn ngày");
            tvTime.setText("--:--");
        }
    }

    @Override
    public void onTaskCheckedChange(Task task, boolean isChecked) {
        taskDao.setTaskDone(task.id, isChecked);
        task.isDone = isChecked;
        syncManager.syncTask(task);
        loadTasks();
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
                    syncManager.deleteTask(task.id);
                    loadTasks();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    selectedFileName = getFileName(selectedFileUri);
                    // Cập nhật UI trong dialog
                    if (currentTvFileName != null) {
                        currentTvFileName.setText(selectedFileName);
                        currentTvFileName.setTextColor(android.graphics.Color.parseColor("#1E293B"));
                    }
                    if (currentBtnClearFile != null) {
                        currentBtnClearFile.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(this, "Đã chọn: " + selectedFileName, Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Hàm bổ trợ lấy tên file từ Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    // 2. Hàm mở trình chọn file (Gắn vào nút "Đính kèm file" trong dialog_add_task)
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Cho phép chọn mọi loại file
        filePickerLauncher.launch(intent);
    }

    // 3. Hàm upload file lên Firebase Storage khi nhấn "Lưu Task"
    private void uploadFileAndSaveTask(Task task) {
        if (selectedFileUri == null) {
            // Nếu không chọn file, lưu task bình thường
            long newId = taskDao.insertTask(task);
            task.id = (int) newId;
            syncManager.syncTask(task);
            loadTasks();
            return;
        }

        File file = new File(task.fileUrl);

        if (!file.exists()) {
            Toast.makeText(this, "Không tìm thấy file", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

                        long newId = taskDao.insertTask(task);
                        task.id = (int) newId;
                        syncManager.syncTask(task);

        startActivity(intent);
    }

}