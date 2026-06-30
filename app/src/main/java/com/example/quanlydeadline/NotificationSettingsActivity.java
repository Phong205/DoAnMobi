package com.example.quanlydeadline;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.NotificationSettingsDao;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.models.NotificationSettings;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Màn Cài đặt thông báo — lưu trong Room (bảng notification_settings).
 * Mỗi toggle gọi NotificationHelper.generateNotifications(tasks, settings)
 * để thực sự ẩn/hiện thông báo tương ứng (đã nối ở DashboardActivity & NotificationActivity).
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private NotificationSettingsDao settingsDao;
    private int currentUserId;
    private NotificationSettings settings;

    private SwitchMaterial switchMaster;
    private TextView tvMasterStatus;

    // Các dòng toggle dùng layout include item_setting_toggle
    private View rowEnableAll, rowSound, rowVibrate;
    private View row10Days, row5Days, row1Day, rowOverdue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        settingsDao = AppDatabase.getDatabase(this).notificationSettingsDao();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        switchMaster = findViewById(R.id.switchMaster);
        tvMasterStatus = findViewById(R.id.tvMasterStatus);

        rowEnableAll = findViewById(R.id.rowEnableAll);
        rowSound = findViewById(R.id.rowSound);
        rowVibrate = findViewById(R.id.rowVibrate);
        row10Days = findViewById(R.id.row10Days);
        row5Days = findViewById(R.id.row5Days);
        row1Day = findViewById(R.id.row1Day);
        rowOverdue = findViewById(R.id.rowOverdue);

        loadSettings();
        bindRowLabels();
        setupListeners();
    }

    private void loadSettings() {
        settings = settingsDao.getSettings(currentUserId);
        if (settings == null) {
            // Chưa có cài đặt → tạo mới với giá trị mặc định
            settings = new NotificationSettings(currentUserId);
            settingsDao.insertOrUpdate(settings);
        }
        applySettingsToUI();
    }

    private void bindRowLabels() {
        setRowContent(rowEnableAll, android.R.drawable.ic_popup_reminder, "Bật thông báo", "Nhận tất cả thông báo từ app");
        setRowContent(rowSound, android.R.drawable.ic_lock_silent_mode_off, "Âm thanh", "Phát âm thanh khi có thông báo");
        setRowContent(rowVibrate, android.R.drawable.ic_lock_silent_mode, "Rung", "Rung khi nhận thông báo");

        setRowContent(row10Days, android.R.drawable.ic_menu_my_calendar, "Còn 10 ngày", "Thông báo sớm trước deadline");
        setRowContent(row5Days, android.R.drawable.ic_dialog_alert, "Còn 5 ngày", "Nhắc nhở khi deadline đang gấp");
        setRowContent(row1Day, android.R.drawable.ic_dialog_alert, "Còn 1 ngày", "Cảnh báo khẩn — deadline ngày mai");
        setRowContent(rowOverdue, android.R.drawable.ic_delete, "Quá hạn", "Thông báo khi task đã hết hạn");
    }

    private void setRowContent(View row, int iconRes, String title, String desc) {
        ImageView icon = row.findViewById(R.id.ivSettingIcon);
        TextView tvTitle = row.findViewById(R.id.tvSettingTitle);
        TextView tvDesc = row.findViewById(R.id.tvSettingDesc);
        icon.setImageResource(iconRes);
        tvTitle.setText(title);
        tvDesc.setText(desc);
    }

    private void applySettingsToUI() {
        switchMaster.setChecked(settings.enableAll);
        updateMasterStatusText();

        getSwitch(rowEnableAll).setChecked(settings.enableAll);
        getSwitch(rowSound).setChecked(settings.sound);
        getSwitch(rowVibrate).setChecked(settings.vibrate);
        getSwitch(row10Days).setChecked(settings.remind10Days);
        getSwitch(row5Days).setChecked(settings.remind5Days);
        getSwitch(row1Day).setChecked(settings.remind1Day);
        getSwitch(rowOverdue).setChecked(settings.remindOverdue);

        // Khi tắt thông báo tổng → disable hết các dòng con cho rõ ràng
        setSubRowsEnabled(settings.enableAll);
    }

    private SwitchMaterial getSwitch(View row) {
        return row.findViewById(R.id.switchSetting);
    }

    private void setupListeners() {
        switchMaster.setOnCheckedChangeListener((btn, checked) -> {
            settings.enableAll = checked;
            getSwitch(rowEnableAll).setChecked(checked);
            setSubRowsEnabled(checked);
            updateMasterStatusText();
            saveSettings();
        });

        getSwitch(rowEnableAll).setOnCheckedChangeListener((btn, checked) -> {
            settings.enableAll = checked;
            switchMaster.setChecked(checked);
            setSubRowsEnabled(checked);
            updateMasterStatusText();
            saveSettings();
        });

        getSwitch(rowSound).setOnCheckedChangeListener((btn, checked) -> {
            settings.sound = checked;
            saveSettings();
        });

        getSwitch(rowVibrate).setOnCheckedChangeListener((btn, checked) -> {
            settings.vibrate = checked;
            saveSettings();
        });

        getSwitch(row10Days).setOnCheckedChangeListener((btn, checked) -> {
            settings.remind10Days = checked;
            saveSettings();
        });

        getSwitch(row5Days).setOnCheckedChangeListener((btn, checked) -> {
            settings.remind5Days = checked;
            saveSettings();
        });

        getSwitch(row1Day).setOnCheckedChangeListener((btn, checked) -> {
            settings.remind1Day = checked;
            saveSettings();
        });

        getSwitch(rowOverdue).setOnCheckedChangeListener((btn, checked) -> {
            settings.remindOverdue = checked;
            saveSettings();
        });
    }

    private void setSubRowsEnabled(boolean enabled) {
        rowSound.setAlpha(enabled ? 1f : 0.4f);
        rowVibrate.setAlpha(enabled ? 1f : 0.4f);
        row10Days.setAlpha(enabled ? 1f : 0.4f);
        row5Days.setAlpha(enabled ? 1f : 0.4f);
        row1Day.setAlpha(enabled ? 1f : 0.4f);
        rowOverdue.setAlpha(enabled ? 1f : 0.4f);

        getSwitch(rowSound).setEnabled(enabled);
        getSwitch(rowVibrate).setEnabled(enabled);
        getSwitch(row10Days).setEnabled(enabled);
        getSwitch(row5Days).setEnabled(enabled);
        getSwitch(row1Day).setEnabled(enabled);
        getSwitch(rowOverdue).setEnabled(enabled);
    }

    private void updateMasterStatusText() {
        tvMasterStatus.setText(settings.enableAll
                ? "Đang bật — bạn sẽ nhận được nhắc nhở"
                : "Đang tắt — bạn sẽ không nhận thông báo nào");
    }

    private void saveSettings() {
        settingsDao.insertOrUpdate(settings);
    }
}