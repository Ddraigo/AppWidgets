package com.example.regreen.myapplication.Admin.New;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.News;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddNewFragment extends Fragment {

    private EditText edtAddNewTitle, edtAddNewCate, edtAddNewDetail, edtDayAdd2;
    private Button btnSaveNew, btnAddPictureNew;
    private ImageView imgPreview;
    private final Calendar calendar = Calendar.getInstance();

    private FirebaseImageHelper imageHelper;
    private String imageUrl; // Để lưu URL của ảnh đã tải lên

    public AddNewFragment() {
    }

    public static AddNewFragment newInstance() {
        return new AddNewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_new, container, false);

        edtAddNewTitle = view.findViewById(R.id.edtAddNewTitle);
        edtAddNewCate = view.findViewById(R.id.edtAddNewCate);
        edtAddNewDetail = view.findViewById(R.id.edtAddNewDetail);
        edtDayAdd2 = view.findViewById(R.id.edtDayAdd2);
        imgPreview = view.findViewById(R.id.imgPreview);
        btnAddPictureNew = view.findViewById(R.id.btnAddPictureNew);
        btnSaveNew = view.findViewById(R.id.btnSaveNew);

        imageHelper = new FirebaseImageHelper(requireContext());

        edtDayAdd2.setOnClickListener(v -> showDatePickerDialog());

        btnAddPictureNew.setOnClickListener(v -> imageHelper.openImagePicker(this));

        btnSaveNew.setOnClickListener(v -> saveNewsToFirebase());

        ImageButton btnBackAddNew = view.findViewById(R.id.btnBackAddNew);
        btnBackAddNew.setOnClickListener(v -> {
            if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateField();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        edtDayAdd2.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý kết quả từ Image Picker
        imageHelper.handleImageResult(requestCode, resultCode, data);

        if (imageHelper.getSelectedImageUri() != null) {
            // Upload ảnh lên Firebase
            imageHelper.uploadImageToRealtimeDatabase(null, new FirebaseImageHelper.OnImageUploadListener() {
                @Override
                public void onSuccess(String base64Image) {
                    // Lưu URL ảnh đã mã hóa Base64
                    imageUrl = base64Image;

                    // Hiển thị ảnh trên giao diện
                    displayBase64Image(base64Image, imgPreview);

                    Toast.makeText(requireContext(), "Tải ảnh thành công!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(requireContext(), "Tải ảnh thất bại: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "Không có ảnh nào được chọn.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNewsToFirebase() {
        String title = edtAddNewTitle.getText().toString().trim();
        String category = edtAddNewCate.getText().toString().trim();
        String detail = edtAddNewDetail.getText().toString().trim();
        String date = edtDayAdd2.getText().toString().trim();

        // Kiểm tra các trường bắt buộc
        if (title.isEmpty() || detail.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng điền tất cả các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.isEmpty()) {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        } else if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(requireContext(), "Định dạng ngày không hợp lệ, vui lòng nhập theo dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo ID duy nhất cho tin tức
        String newId = "news_" + System.currentTimeMillis();

        // Tạo đối tượng News
        News news = new News(newId, category, title, detail, imageUrl, date);

        // Lưu tin tức vào Firebase
        FirebaseRepository<News> repository = new FirebaseRepository<>("News", News.class);
        repository.save(newId, news, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(requireContext(), "Thêm tin tức thành công!", Toast.LENGTH_SHORT).show();
                clearFields(); // Xóa dữ liệu sau khi lưu thành công
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(requireContext(), "Không thể thêm tin tức: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("AddNewFragment", "Lỗi giải mã Base64: " + e.getMessage());
                imageView.setImageResource(R.drawable._981712_scaled); // Ảnh mặc định nếu lỗi
            }
        } else {
            imageView.setImageResource(R.drawable._981712_scaled); // Ảnh mặc định nếu không có dữ liệu
        }
    }

    private void clearFields() {
        edtAddNewTitle.setText("");
        edtAddNewCate.setText("");
        edtAddNewDetail.setText("");
        edtDayAdd2.setText("");
        imgPreview.setImageResource(R.drawable._981712_scaled);
        imageUrl = null;
    }

}
