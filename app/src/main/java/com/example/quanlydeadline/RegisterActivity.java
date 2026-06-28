package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilPassword, tilConfirmPassword;
    private TextInputEditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton btnRegister, btnGoogleRegister;
    private TextView tvGoToLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // 1. Ánh xạ view
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // 2. Xử lý sự kiện bấm nút Đăng ký
        btnRegister.setOnClickListener(v -> handleRegister());

        // 3. Chuyển sang màn Đăng nhập
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        // Bước 0: Xóa các thông báo lỗi cũ (nếu có) trước khi kiểm tra lại
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Bước 1: Kiểm tra rỗng
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tất cả thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 2: Kiểm tra định dạng Email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Định dạng Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            edtEmail.requestFocus();
            return;
        }

        // Bước 3: Kiểm tra độ mạnh Mật khẩu (1 Hoa, 1 thường, 1 số, 1 đặc biệt, >=8 ký tự)
        if (!isValidPassword(password)) {
            tilPassword.setError("Mật khẩu yếu! Cần >=8 ký tự gồm chữ Hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&)");
            edtPassword.requestFocus();
            return;
        }

        // Bước 4: Kiểm tra khớp mật khẩu
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
            edtConfirmPassword.requestFocus();
            return;
        }

        // Bước 5: Gọi Firebase tạo tài khoản
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển thẳng vào màn chính sau khi đăng ký xong
                    Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    String errorMsg = "Đăng ký thất bại: " + e.getMessage();
                    if (e.getMessage() != null && e.getMessage().contains("email address is already in use")) {
                        errorMsg = "Email này đã được sử dụng cho một tài khoản khác!";
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                });
    }

    // Hàm phụ trợ Regex
    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password != null && password.matches(passwordRegex);
    }
}