package com.example.deadlineapp.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


import com.example.quanlydeadline.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class AddTaskDialog extends DialogFragment {

    // ── Views ─────────────────────────────────────────────────────────────
    private TextInputEditText edtTaskTitle;
    private MaterialButton    btnAttachFile;
    private TextView          tvAttachedFileName;
    private ImageButton       btnClearFile;
    private Spinner           spinnerProject;
    private TextView          btnPriorityLow;
    private TextView          btnPriorityMedium;
    private TextView          btnPriorityHigh;
    private View              tvPickTaskDueDate;
    private TextView          tvDeadlineDate;
    private TextView          tvDeadlineTime;
    private View              tvPickReminder;
    private TextView          tvReminderDate;
    private TextView          tvReminderTime;
    private SeekBar           seekBarProgress;
    private TextView          tvProgressPercent;
    private TextInputEditText edtTaskNote;

    // ── State ─────────────────────────────────────────────────────────────
    private Uri      attachedFileUri  = null;   // null = chưa chọn file
    private String   selectedPriority = "MEDIUM";
    private Calendar deadlineCal      = Calendar.getInstance();
    private Calendar reminderCal      = Calendar.getInstance();

    // ── File picker (ActivityResultLauncher — thay thế startActivityForResult) ──
    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Được gọi khi người dùng chọn xong file (hoặc hủy)
                        if (result.getData() != null && result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            onFilePicked(uri);
                        }
                    }
            );

    // ─────────────────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupUploadFile();      // ← PHẦN UPLOAD FILE
        setupSpinner();
        setupPriority();
        setupDateTimePickers();
        setupSeekBar();
    }

    // ── Bind tất cả view từ XML ───────────────────────────────────────────
    private void bindViews(View view) {
        edtTaskTitle       = view.findViewById(R.id.edtTaskTitle);
        btnAttachFile      = view.findViewById(R.id.btnAttachFile);
        tvAttachedFileName = view.findViewById(R.id.tvAttachedFileName);
        btnClearFile       = view.findViewById(R.id.btnClearFile);
        spinnerProject     = view.findViewById(R.id.spinnerProject);
        btnPriorityLow     = view.findViewById(R.id.btnPriorityLow);
        btnPriorityMedium  = view.findViewById(R.id.btnPriorityMedium);
        btnPriorityHigh    = view.findViewById(R.id.btnPriorityHigh);
        tvPickTaskDueDate  = view.findViewById(R.id.tvPickTaskDueDate);
        tvDeadlineDate     = view.findViewById(R.id.tvDeadlineDate);
        tvDeadlineTime     = view.findViewById(R.id.tvDeadlineTime);
        tvPickReminder     = view.findViewById(R.id.tvPickReminder);
        tvReminderDate     = view.findViewById(R.id.tvReminderDate);
        tvReminderTime     = view.findViewById(R.id.tvReminderTime);
        seekBarProgress    = view.findViewById(R.id.seekBarProgress);
        tvProgressPercent  = view.findViewById(R.id.tvProgressPercent);
        edtTaskNote        = view.findViewById(R.id.edtTaskNote);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UPLOAD FILE — toàn bộ logic ở đây
    // ══════════════════════════════════════════════════════════════════════
    private void setupUploadFile() {

        // BƯỚC 1: Click "Chọn File" → mở file picker hệ thống
        btnAttachFile.setOnClickListener(v -> openFilePicker());

        // BƯỚC 2: Click nút X → xóa file đang đính kèm
        btnClearFile.setOnClickListener(v -> clearAttachedFile());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");                           // mọi loại file
        intent.addCategory(Intent.CATEGORY_OPENABLE);   // chỉ lấy file có thể mở
        // Nếu muốn giới hạn nhiều loại cụ thể:
        // intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/pdf","image/*","application/msword"});
        filePickerLauncher.launch(intent);
    }

    private void onFilePicked(Uri uri) {
        attachedFileUri = uri;

        // Lấy tên file + kích thước từ ContentResolver
        String fileName = getFileNameFromUri(uri);
        String fileSize = getFileSizeFromUri(uri);

        // Hiển thị tên file lên tvAttachedFileName
        tvAttachedFileName.setText(fileName + " · " + fileSize);
        tvAttachedFileName.setTextColor(
                requireContext().getColor(android.R.color.black)
        );

        // Hiện nút X để xóa file
        btnClearFile.setVisibility(View.VISIBLE);
    }

    private void clearAttachedFile() {
        attachedFileUri = null;
        tvAttachedFileName.setText("Chưa có file nào");
        tvAttachedFileName.setTextColor(
                requireContext().getColor(android.R.color.darker_gray)
        );
        btnClearFile.setVisibility(View.GONE);
    }

    private String getFileNameFromUri(Uri uri) {
        String name = "unknown_file";
        try (Cursor cursor = requireContext().getContentResolver()
                .query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = cursor.getString(idx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    private String getFileSizeFromUri(Uri uri) {
        long bytes = 0;
        try (Cursor cursor = requireContext().getContentResolver()
                .query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (idx >= 0 && !cursor.isNull(idx)) {
                    bytes = cursor.getLong(idx);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bytes >= 1_048_576) return String.format(Locale.getDefault(), "%.1f MB", bytes / 1_048_576.0);
        if (bytes >= 1_024)     return (bytes / 1_024) + " KB";
        return bytes + " B";
    }

    // ── Lấy Uri file khi cần lưu (gọi từ nút Lưu) ───────────────────────
    @Nullable
    public Uri getAttachedFileUri() {
        return attachedFileUri;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CÁC PHẦN KHÁC (Priority, DateTime, SeekBar, Spinner)
    // ══════════════════════════════════════════════════════════════════════

    private void setupSpinner() {
        List<String> projects = Arrays.asList(
                "Lập Trình Mobile", "Cơ Sở Dữ Liệu", "Kỹ Thuật Phần Mềm"
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                projects
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(adapter);
    }

    private void setupPriority() {
        btnPriorityLow.setOnClickListener(v    -> setPriority("LOW"));
        btnPriorityMedium.setOnClickListener(v -> setPriority("MEDIUM"));
        btnPriorityHigh.setOnClickListener(v   -> setPriority("HIGH"));
        setPriority("MEDIUM"); // default
    }

    private void setPriority(String priority) {
        selectedPriority = priority;
        // Reset tất cả về unselected
        btnPriorityLow.setBackgroundResource(R.drawable.priority_unselected_low);
        btnPriorityLow.setTextColor(requireContext().getColor(android.R.color.holo_green_dark));

        btnPriorityMedium.setBackgroundResource(R.drawable.priority_unselected_medium);
        btnPriorityMedium.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark));

        btnPriorityHigh.setBackgroundResource(R.drawable.priority_unselected_high);
        btnPriorityHigh.setTextColor(requireContext().getColor(android.R.color.holo_red_dark));

        // Highlight nút được chọn
        switch (priority) {
            case "LOW":
                btnPriorityLow.setBackgroundResource(R.drawable.priority_selected_low);
                btnPriorityLow.setTextColor(requireContext().getColor(android.R.color.white));
                break;
            case "MEDIUM":
                btnPriorityMedium.setBackgroundResource(R.drawable.priority_selected_medium);
                btnPriorityMedium.setTextColor(requireContext().getColor(android.R.color.white));
                break;
            case "HIGH":
                btnPriorityHigh.setBackgroundResource(R.drawable.priority_selected_high);
                btnPriorityHigh.setTextColor(requireContext().getColor(android.R.color.white));
                break;
        }
    }

    private void setupDateTimePickers() {
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Deadline: click RelativeLayout tvPickTaskDueDate
        tvPickTaskDueDate.setOnClickListener(v ->
                showDateTimePicker(deadlineCal, tvDeadlineDate, tvDeadlineTime, dateFmt, timeFmt)
        );

        // Nhắc nhở: click RelativeLayout tvPickReminder
        tvPickReminder.setOnClickListener(v ->
                showDateTimePicker(reminderCal, tvReminderDate, tvReminderTime, dateFmt, timeFmt)
        );
    }

    private void showDateTimePicker(Calendar cal,
                                    TextView tvDate, TextView tvTime,
                                    SimpleDateFormat dateFmt, SimpleDateFormat timeFmt) {
        new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    cal.set(year, month, day);
                    tvDate.setText(dateFmt.format(cal.getTime()));

                    // Sau khi chọn ngày → mở chọn giờ
                    new TimePickerDialog(requireContext(),
                            (tp, hour, min) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hour);
                                cal.set(Calendar.MINUTE, min);
                                tvTime.setText(timeFmt.format(cal.getTime()));
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void setupSeekBar() {
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgressPercent.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // ── Tạo dialog instance ───────────────────────────────────────────────
    public static AddTaskDialog newInstance() {
        return new AddTaskDialog();
    }

    // ── Mở rộng dialog chiều ngang khi show ──────────────────────────────
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}