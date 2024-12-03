package com.example.regreen.myapplication.Admin.Reward;

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
import com.example.regreen.myapplication.ModelData.Reward;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddRewardFragment extends Fragment {

    private EditText edtAddRewardCate, edtAddRewardTitle, edtAddRewardCon, edtAddRewardPoint, edtDayAddReward;
    private Button btnSaveReward, btnAddPictureReward;
    private ImageView imgRewardPreview;
    private final Calendar calendar = Calendar.getInstance();

    private FirebaseImageHelper imageHelper;
    private String imageUrl;

    public AddRewardFragment() {}

    public static AddRewardFragment rewardInstance() {
        return new AddRewardFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_reward, container, false);

        edtAddRewardCate = view.findViewById(R.id.edtAddRewardCate);
        edtAddRewardTitle = view.findViewById(R.id.edtAddRewardTitle);
        edtAddRewardCon = view.findViewById(R.id.edtAddRewardCon);
        edtAddRewardPoint = view.findViewById(R.id.edtAddRewardPoint);
        edtDayAddReward = view.findViewById(R.id.edtDayAddReward);
        imgRewardPreview = view.findViewById(R.id.imgRewardPreview);
        btnAddPictureReward = view.findViewById(R.id.btnAddPictureReward);
        btnSaveReward = view.findViewById(R.id.btnSaveReward);

        imageHelper = new FirebaseImageHelper(requireContext());

        edtDayAddReward.setOnClickListener(v -> showDatePickerDialog());

        btnAddPictureReward.setOnClickListener(v -> imageHelper.openImagePicker(this));

        btnSaveReward.setOnClickListener(v -> saveRewardToFirebase());

        ImageButton btnBackAddReward = view.findViewById(R.id.btnBackAddReward);
        btnBackAddReward.setOnClickListener(v -> {
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
        edtDayAddReward.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý kết quả từ Image Picker
        imageHelper.handleImageResult(requestCode, resultCode, data);

        if (imageHelper.getSelectedImageUri() != null) {
            // Upload ảnh lên Firebase
            imageHelper.uploadImageToRealtimeDatabase("uploads/rewards", new FirebaseImageHelper.OnImageUploadListener() {
                @Override
                public void onSuccess(String base64Image) {
                    // Lưu URL ảnh mã hóa Base64
                    imageUrl = base64Image;

                    // Hiển thị ảnh trên giao diện
                    displayBase64Image(base64Image, imgRewardPreview);

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

    private void saveRewardToFirebase() {
        String title = edtAddRewardTitle.getText().toString().trim();
        String category = edtAddRewardCate.getText().toString().trim();
        String condition = edtAddRewardCon.getText().toString().trim();
        String pointStr = edtAddRewardPoint.getText().toString().trim();
        String date = edtDayAddReward.getText().toString().trim();

        // Kiểm tra các trường bắt buộc
        if (title.isEmpty() || category.isEmpty() || condition.isEmpty() || pointStr.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng điền tất cả các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        int point;
        try {
            point = Integer.parseInt(pointStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Điểm thưởng phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.isEmpty()) {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        } else if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(requireContext(), "Định dạng ngày không hợp lệ, vui lòng nhập theo dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo ID duy nhất cho phần thưởng
        String rewardId = "reward_" + System.currentTimeMillis();

        // Tạo đối tượng Reward
        Reward reward = new Reward(rewardId, point, category, title, condition, imageUrl, date);

        // Lưu phần thưởng vào Firebase
        FirebaseRepository<Reward> repository = new FirebaseRepository<>("Reward", Reward.class);
        repository.save(rewardId, reward, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(requireContext(), "Thêm phần thưởng thành công!", Toast.LENGTH_SHORT).show();
                clearFields(); // Xóa dữ liệu sau khi lưu thành công
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(requireContext(), "Không thể thêm phần thưởng: " + message, Toast.LENGTH_SHORT).show();
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
                Log.e("AddRewardFragment", "Lỗi giải mã Base64: " + e.getMessage());
                imageView.setImageResource(R.drawable._981712_scaled); // Ảnh mặc định nếu lỗi giải mã
            }
        } else {
            imageView.setImageResource(R.drawable._981712_scaled); // Ảnh mặc định nếu không có dữ liệu
        }
    }

    private void clearFields() {
        edtAddRewardTitle.setText("");
        edtAddRewardCate.setText("");
        edtAddRewardCon.setText("");
        edtAddRewardPoint.setText("");
        edtDayAddReward.setText("");
        imgRewardPreview.setImageResource(R.drawable._981712_scaled);
        imageUrl = null;
    }
}
