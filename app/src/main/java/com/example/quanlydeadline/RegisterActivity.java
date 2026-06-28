package com.example.quanlydeadline;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.quanlydeadline.database.SessionManager;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton btnRegister, btnGoogleRegister;
    private TextView tvGoToLogin;

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private SessionManager sessionManager;

    private static final String WEB_CLIENT_ID = "84756653124-2ne720a8npfp0r63bc6397rqbmsgetgl.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);
        sessionManager = new SessionManager(this);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Đăng ký truyền thống
        btnRegister.setOnClickListener(v -> registerWithEmail());

        // Đăng ký bằng Google
        btnGoogleRegister.setOnClickListener(v -> registerWithGoogle());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    // ================= 1. ĐĂNG KÝ BẰNG EMAIL / PASSWORD =================

    private void registerWithEmail() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        // Cập nhật Họ Tên lên Firebase Profile
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();

                        user.updateProfile(profileUpdate).addOnCompleteListener(task -> {
                            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMsg = "Đăng ký thất bại";
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("email address is already in use")) {
                            errorMsg = "Email này đã được sử dụng";
                        } else if (e.getMessage().contains("badly formatted")) {
                            errorMsg = "Định dạng email không hợp lệ";
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                });
    }

    // ================= 2. ĐĂNG KÝ BẰNG GOOGLE =================

    private void registerWithGoogle() {
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
                        handleGoogleRegisterResult(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Toast.makeText(RegisterActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void handleGoogleRegisterResult(GetCredentialResponse result) {
        try {
            GoogleIdTokenCredential credential = GoogleIdTokenCredential.createFrom(result.getCredential().getData());
            String idToken = credential.getIdToken();

            AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);

            mAuth.signInWithCredential(authCredential)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            String fullName = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();

                            // ✅ Đăng ký Google xong là tự động đăng nhập thẳng vào Dashboard luôn
                            sessionManager.saveSession(user.getUid().hashCode(), fullName);

                            Toast.makeText(this, "Đăng ký tài khoản Google thành công!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                            intent.putExtra("FULL_NAME", fullName);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show()
                    );

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xác thực tài khoản Google", Toast.LENGTH_SHORT).show();
        }
    }
}