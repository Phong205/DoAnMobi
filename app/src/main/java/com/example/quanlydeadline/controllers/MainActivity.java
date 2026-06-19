package com.example.quanlydeadline.controllers;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlydeadline.database.SessionManager;

/**
 * MainActivity hiện đóng vai trò "cổng vào" sau khi đăng nhập:
 * - Nếu đã đăng nhập -> chuyển thẳng vào màn hình quản lý đồ án (ProjectListActivity).
 * - Nếu chưa đăng nhập (trường hợp mở thẳng MainActivity) -> quay lại LoginActivity.
 *
 * Khi cần, có thể thay phần điều hướng này bằng giao diện trang chủ (activity_main.xml) riêng,
 * ví dụ thêm Bottom Navigation, thống kê tổng quan, v.v.
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main); // Tạm ẩn vì chưa có giao diện main riêng

        SessionManager sessionManager = new SessionManager(this);
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(this, ProjectListActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
