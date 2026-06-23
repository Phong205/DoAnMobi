package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.DatabaseClient;
import com.example.quanlydeadline.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtFullName;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtConfirmPassword;

    private MaterialButton btnRegister;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword =
                findViewById(R.id.edtConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            RegisterActivity.this,
                            LoginActivity.class);

            startActivity(intent);

            finish();
        });
    }

    private void registerUser() {

        String fullName =
                edtFullName.getText().toString().trim();

        String email =
                edtEmail.getText().toString().trim();

        String password =
                edtPassword.getText().toString().trim();

        String confirmPassword =
                edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!password.equals(confirmPassword)) {

            Toast.makeText(
                    this,
                    "Mật khẩu xác nhận không khớp",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        AppDatabase db =
                DatabaseClient.getInstance(this);

        User existingUser =
                db.userDao().getUserByEmail(email);

        if (existingUser != null) {

            Toast.makeText(
                    this,
                    "Email đã tồn tại",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        User user =
                new User(
                        fullName,
                        email,
                        password
                );

        db.userDao().insertUser(user);

        Toast.makeText(
                this,
                "Đăng ký thành công",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent =
                new Intent(
                        RegisterActivity.this,
                        LoginActivity.class);

        startActivity(intent);

        finish();
    }
}