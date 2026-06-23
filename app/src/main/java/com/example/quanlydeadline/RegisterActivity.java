package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtFullName;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtConfirmPassword;

    private MaterialButton btnRegister;
    private TextView tvGoToLogin;

    // ✅ Thay Room bằng Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance(); // ✅ khởi tạo Firebase Auth

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {

        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Kiểm tra rỗng
        if (fullName.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mật khẩu khớp
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra độ dài mật khẩu (Firebase yêu cầu tối thiểu 6 ký tự)
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Đăng ký qua Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    // ✅ Lưu displayName lên Firebase
                    com.google.firebase.auth.UserProfileChangeRequest profileUpdate =new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build();

                    result.getUser().updateProfile(profileUpdate)
                            .addOnCompleteListener(task -> {
                                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi phổ biến
                    String errorMsg = "Đăng ký thất bại";
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("email address is already in use")) {
                            errorMsg = "Email đã được sử dụng";
                        } else if (e.getMessage().contains("badly formatted")) {
                            errorMsg = "Email không hợp lệ";
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                });
    }
}