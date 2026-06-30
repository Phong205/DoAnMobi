package com.example.quanlydeadline;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Màn Chỉnh sửa thông tin — lưu lên Firebase.
 * - fullName: lưu vào FirebaseAuth profile (displayName)
 * - phone, birthday: lưu vào Firestore collection "user_profiles" vì
 *   FirebaseAuth profile không có field cho 2 thông tin này.
 */
public class EditProfileActivity extends AppCompatActivity {

    private ShapeableImageView imgAvatarEdit;
    private TextInputEditText edtFullName, edtEmail, edtPhone;
    private TextView tvBirthday, tvChangeAvatar, tvCancelEdit;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private long selectedBirthday = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initViews();
        loadCurrentData();
        setupEvents();
    }

    private void initViews() {
        imgAvatarEdit = findViewById(R.id.imgAvatarEdit);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        tvBirthday = findViewById(R.id.tvBirthday);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);
        tvCancelEdit = findViewById(R.id.tvCancelEdit);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadCurrentData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtFullName.setText(user.getDisplayName());
        edtEmail.setText(user.getEmail());

        // ✅ Lấy phone + birthday từ Firestore (Auth không lưu được 2 field này)
        firestore.collection("user_profiles")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String phone = doc.getString("phone");
                        Long birthday = doc.getLong("birthday");

                        if (phone != null) edtPhone.setText(phone);
                        if (birthday != null && birthday > 0) {
                            selectedBirthday = birthday;
                            updateBirthdayLabel();
                        }
                    }
                });
    }

    private void setupEvents() {
        tvChangeAvatar.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng đổi ảnh đại diện đang phát triển", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.layoutPickBirthday).setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());

        tvCancelEdit.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedBirthday > 0) calendar.setTimeInMillis(selectedBirthday);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, 0, 0, 0);
            selectedBirthday = picked.getTimeInMillis();
            updateBirthdayLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateBirthdayLabel() {
        tvBirthday.setText(android.text.format.DateFormat.format("dd/MM/yyyy", selectedBirthday));
    }

    private void saveProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ và tên", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // ✅ 1. Cập nhật displayName trên FirebaseAuth
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        user.updateProfile(profileUpdate).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // ✅ 2. Lưu phone + birthday vào Firestore
                Map<String, Object> data = new HashMap<>();
                data.put("fullName", fullName);
                data.put("phone", phone);
                data.put("birthday", selectedBirthday);

                firestore.collection("user_profiles")
                        .document(user.getUid())
                        .set(data)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật tên", Toast.LENGTH_SHORT).show();
            }
        });
    }
}