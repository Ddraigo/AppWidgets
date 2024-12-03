package com.example.regreen.myapplication.Admin.GenericMethod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.regreen.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FirebaseImageHelper {

    public static final int PICK_IMAGE_REQUEST = 100;
    private final Context context;
    private Uri selectedImageUri;
    private String base64Image;

    public FirebaseImageHelper(Context context) {
        this.context = context;
    }

    public void openImagePicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(intent, "Chọn Ảnh"), PICK_IMAGE_REQUEST);
    }

    public void openImagePicker(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        fragment.startActivityForResult(Intent.createChooser(intent, "Chọn Ảnh"), PICK_IMAGE_REQUEST);
    }

    public void handleImageResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
        }
    }


    public Uri getSelectedImageUri() {
        return selectedImageUri;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void uploadImageToRealtimeDatabase(String databasePath, OnImageUploadListener listener) {
        if (selectedImageUri == null) {
            listener.onFailure("Vui lòng chọn ảnh trước khi tải lên");
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImageUri);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Compress image with 70% quality
            byte[] byteArray = baos.toByteArray();
            base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            listener.onSuccess(base64Image); // Directly return Base64 to save in News object

        } catch (IOException e) {
            listener.onFailure("Không thể chuyển đổi ảnh: " + e.getMessage());
            Log.e("FirebaseImageHelper", "Lỗi chuyển đổi ảnh sang Base64", e);
        }
    }

    public interface OnImageUploadListener {
        void onSuccess(String message);
        void onFailure(String message);
    }

    public void loadImageFromRealtimeDatabase(String databasePath, ImageView imageView) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String base64Image = snapshot.getValue(String.class);

                if (base64Image != null && !base64Image.isEmpty()) {
                    try {
                        // Chuyển Base64 về Bitmap
                        byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        imageView.setImageBitmap(bitmap);
                    } catch (IllegalArgumentException e) {
                        Log.e("FirebaseImageHelper", "Base64 không hợp lệ, hiển thị ảnh mặc định", e);
                        imageView.setImageResource(R.drawable._981712_scaled); // Ảnh mặc định nếu lỗi
                    }
                } else {
                    imageView.setImageResource(R.drawable._981712_scaled); // Ảnh mặc định nếu không có dữ liệu
                    Toast.makeText(context, "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Lỗi tải ảnh từ Realtime Database: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseImageHelper", "Error loading image from Firebase Realtime Database", error.toException());
            }
        });
    }

}
