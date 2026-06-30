package com.example.quanlydeadline;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.ProjectDao;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.database.TaskDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView imgAvatar;
    private TextView tvFullName, tvEmail, tvProjectCount, tvDeadlineDoneCount;
    private LinearLayout btnEditProfile, btnChangePassword, btnNotificationSettings;
    private MaterialButton btnSwitchAccount, btnLogout;
    private BottomNavigationView bottomNav;

    private FirebaseAuth mAuth;
    private SessionManager sessionManager;
    private ProjectDao projectDao;
    private TaskDao taskDao;
    // Launcher Camera
    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    imgAvatar.setImageBitmap(bitmap);
                    Toast.makeText(this, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Launcher Thư viện
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imgAvatar.setImageURI(uri);
                    Toast.makeText(this, "Đã cập nhật ảnh từ thư viện!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this); // Khởi tạo Session
        projectDao = AppDatabase.getDatabase(this).projectDao();
        taskDao = AppDatabase.getDatabase(this).taskDao();

        initViews();
        loadUserData();
        setupEvents();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Cập nhật lại số liệu mỗi khi quay về Profile
        loadStats();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvProjectCount = findViewById(R.id.tvProjectCount);
        tvDeadlineDoneCount = findViewById(R.id.tvDeadlineDoneCount);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnNotificationSettings = findViewById(R.id.btnNotificationSettings);

        btnSwitchAccount = findViewById(R.id.btnSwitchAccount);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());

            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = user.getEmail();
                name = (email != null && email.contains("@")) ? email.substring(0, email.indexOf("@")) : "Người dùng";
            }
            tvFullName.setText(name);

            // ✅ Lấy số liệu thật từ Room thay vì set cứng
            loadStats();
        }
    }

    private void loadStats() {
        int userId = sessionManager.getUserId();

        // Tổng số đồ án của user
        int totalProjects = projectDao.getProjectsByUser(userId).size();
        tvProjectCount.setText(String.valueOf(totalProjects));

        // Tổng số deadline (task) đã hoàn thành của user
        int totalDone = taskDao.getDoneTasks(userId).size();
        tvDeadlineDoneCount.setText(String.valueOf(totalDone));
    }

    private void setupEvents() {
        imgAvatar.setOnClickListener(v -> showImagePickDialog());
        btnLogout.setOnClickListener(v -> handleLogout());
        btnSwitchAccount.setOnClickListener(v -> handleSwitchAccount());

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        btnNotificationSettings.setOnClickListener(v -> startActivity(new Intent(this, NotificationSettingsActivity.class)));
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0, 0);
                finish();
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
            }
            return false;
        });
    }

    private void showImagePickDialog() {
        String[] options = {"📷  Chụp ảnh trực tiếp", "🖼️  Chọn từ Thư viện"};

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) takePictureLauncher.launch(null);
                    else pickImageLauncher.launch("image/*");
                })
                .show();
    }

    private void handleLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn thoát khỏi tài khoản này?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performCleanLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleSwitchAccount() {
        Toast.makeText(this, "Vui lòng đăng nhập tài khoản mới", Toast.LENGTH_SHORT).show();
        performCleanLogout();
    }

    private void performCleanLogout() {
        // Đăng xuất khỏi server Firebase
        mAuth.signOut();

        // Xóa ID người dùng lưu trong SharedPreferences dưới máy
        if (sessionManager != null) {
            sessionManager.clearSession();
        }

        // Chỉ đích danh LoginActivity là điểm đến, cấm đi lạc
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}