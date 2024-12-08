package com.example.regreen.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;

import com.example.regreen.myapplication.ModelData.User;
import com.example.regreen.myapplication.User.UserBooking;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private EditText edtName, edtEmail, edtAddress, edtPhone;
    private ImageView imgAvatar;
    private TextView tvUserNameProfile;
    private FirebaseRepository<User> userRepository;
    private FirebaseImageHelper imageHelper;
    private String base64Avatar;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getWindow().setStatusBarColor(getResources().getColor(R.color.green_bg, null));

        sharedPreferences = EditProfile.this.getSharedPreferences(Login.PREF_NAME, Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "Guest");
        
        String userEmail = getIntent().getStringExtra("userEmail");
        if (userEmail == null && userName == null) {
            Toast.makeText(this, "Không thể xác định người dùng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRepository = new FirebaseRepository<>("User", User.class);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtAddress = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        tvUserNameProfile = findViewById(R.id.tvUserNameProfile);
        imgAvatar = findViewById(R.id.imgAvatar);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        ImageButton btnCapture = findViewById(R.id.btnCapture);
        ImageButton btnBackEditProfile = findViewById(R.id.btnBackEditProfile);

        imageHelper = new FirebaseImageHelper(this);

        loadUserProfile(userEmail);

        btnCapture.setOnClickListener(v -> imageHelper.openImagePicker(this));
        btnSaveProfile.setOnClickListener(v -> saveProfile(userEmail));
        btnBackEditProfile.setOnClickListener(v -> finish());
    }

    private void saveUserName(String userName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", userName);
        editor.apply();
    }


    private void loadUserProfile(String userEmail) {

        if (userEmail == null) {
            Toast.makeText(this, "Không thể xác định người dùng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        

        String emailPath = userEmail.replace(".", ",");
        userRepository.get(emailPath, new FirebaseRepository.OnFetchListener<User>() {
            @Override
            public void onSuccess(User user) {
                edtName.setText(user.getName());
                edtEmail.setText(user.getEmail());
                edtAddress.setText(user.getAddress());
                edtPhone.setText(user.getNumberPhone());
                tvUserNameProfile.setText(user.getName());

                if (user.getAvatar() != null) {
                    displayBase64Image(user.getAvatar(), imgAvatar);
                    base64Avatar = user.getAvatar();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(EditProfile.this, "Không thể tải hồ sơ: " + message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveProfile(String userEmail) {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();


        if (!userEmail.equals(email)) {
            Toast.makeText(this, "Không thể chỉnh sửa email của người dùng khác!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (base64Avatar == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
            return;
        }

        String emailPath = userEmail.replace(".", ",");
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("address", address);
        updates.put("numberPhone", phone);
        updates.put("avatar", base64Avatar);

        userRepository.update(emailPath, updates, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(EditProfile.this, "Hồ sơ đã được cập nhật thành công!", Toast.LENGTH_SHORT).show();
                saveUserName(name);

                // Gửi broadcast để widget cập nhật lại
                Intent intent = new Intent("com.example.regreen.USER_PROFILE_UPDATED");
                sendBroadcast(intent);

                finish();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(EditProfile.this, "Cập nhật thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FirebaseImageHelper.PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageHelper.handleImageResult(requestCode, resultCode, data);

            imageHelper.uploadImageToRealtimeDatabase("temp", new FirebaseImageHelper.OnImageUploadListener() {
                @Override
                public void onSuccess(String base64Image) {
                    base64Avatar = base64Image;
                    displayBase64Image(base64Avatar, imgAvatar);
                    Toast.makeText(EditProfile.this, "Ảnh được tải lên thành công!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(EditProfile.this, "Lỗi tải ảnh: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Lỗi giải mã Base64", Toast.LENGTH_SHORT).show();
                imageView.setImageResource(R.drawable._981712_scaled);
            }
        } else {
            imageView.setImageResource(R.drawable._981712_scaled);
        }
    }
}

