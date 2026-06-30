package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.quanlydeadline.database.AppDatabase;
import com.example.quanlydeadline.database.SessionManager;
import com.example.quanlydeadline.models.User;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoogleLogin;
    private TextView tvGoToRegister;
    private SessionManager sessionManager;
    private FirebaseAuth mAuth;

    private CredentialManager credentialManager;
    private TextView tvForgotPassword;

    private static final String WEB_CLIENT_ID = "84756653124-2ne720a8npfp0r63bc6397rqbmsgetgl.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this); // ✅ Khởi tạo CredentialManager

        // Nếu đã đăng nhập rồi thì skip thẳng vào Dashboard
        if (sessionManager.isLoggedIn()) {
            goToDashboard(sessionManager.getFullName());
            return;
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        btnLogin.setOnClickListener(v -> loginUser());

        // ✅ Sự kiện click nút Google
        btnGoogleLogin.setOnClickListener(v -> loginWithGoogle());

        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    String fullName = user.getDisplayName() != null ? user.getDisplayName() : email;

                    sessionManager.saveSession(Math.abs(user.getUid().hashCode()), fullName);

                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    goToDashboard(fullName);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= XỬ LÝ ĐĂNG NHẬP GOOGLE =================

    private void loginWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignInResult(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Toast.makeText(LoginActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void handleGoogleSignInResult(GetCredentialResponse result) {
        try {
            GoogleIdTokenCredential credential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
            String idToken = credential.getIdToken();

            AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);

            mAuth.signInWithCredential(authCredential)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            String fullName = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();

                            int userId = Math.abs(user.getUid().hashCode());
                            AppDatabase db = AppDatabase.getDatabase(this);
                            com.example.quanlydeadline.models.User existingUser = db.userDao().getUserById(userId);
                            if (existingUser == null) {
                                User newUser =
                                        new User(fullName, user.getEmail(), "");
                                newUser.id = userId;
                                db.userDao().insertUser(newUser);
                            }

                            sessionManager.saveSession(userId, fullName);

                            Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();
                            goToDashboard(fullName);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show()
                    );

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xác thực tài khoản Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToDashboard(String fullName) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("FULL_NAME", fullName);
        startActivity(intent);
        finish();
    }
}