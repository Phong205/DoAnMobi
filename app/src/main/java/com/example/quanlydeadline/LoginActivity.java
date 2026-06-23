package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.DatabaseClient;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private TextView tvGoToRegister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // ✅ Nếu đã đăng nhập rồi thì skip thẳng vào Dashboard
        if (sessionManager.isLoggedIn()) {
            goToDashboard(sessionManager.getFullName());
            return;
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void loginUser() {

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = DatabaseClient.getInstance(this);
        User user = db.userDao().login(email, password);

        if (user == null) {
            Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Lưu session — đây là dòng bị thiếu trước đây
        sessionManager.saveSession(user.id, user.fullName);

        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

        goToDashboard(user.fullName);
    }

    private void goToDashboard(String fullName) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("FULL_NAME", fullName);
        startActivity(intent);
        finish();
    }
}