package com.example.regreen.myapplication.Admin.Reward;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.Admin.New.NewDetailFragment;
import com.example.regreen.myapplication.ModelData.News;
import com.example.regreen.myapplication.ModelData.Reward;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RewardDetailFragment extends BottomSheetDialogFragment {

    private String idReward, title, condition, imageResource, category, dateUp;
    private int point;
    private final Calendar calendar = Calendar.getInstance();
    private FirebaseRepository<Reward> repository;
    private FirebaseImageHelper imageHelper;
    private EditText edtRewardTitle, edtRewardId, edtRewardCondition, edtRewardCate, edtRewardPoint, edtDayAddReward;
    private ImageView imgEditReward;
    private String uploadedImageBase64;

    public RewardDetailFragment() {}

    public static RewardDetailFragment newInstance(String idReward, int point, String category, String title, String condition, String imageResource, String dateUp) {
        RewardDetailFragment fragment = new RewardDetailFragment();
        Bundle args = new Bundle();
        args.putString("idReward", idReward);
        args.putInt("point", point);
        args.putString("category", category);
        args.putString("title", title);
        args.putString("condition", condition);
        args.putString("imageResource", imageResource);
        args.putString("dateUp", dateUp);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idReward = getArguments().getString("idReward");
            point = getArguments().getInt("point");
            category = getArguments().getString("category");
            title = getArguments().getString("title");
            condition = getArguments().getString("condition");
            imageResource = getArguments().getString("imageResource");
            dateUp = getArguments().getString("dateUp");
        }

        repository = new FirebaseRepository<>("Reward", Reward.class);
        imageHelper = new FirebaseImageHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reward_detail, container, false);
        setupUI(view);
        populateData();
        return view;
    }

    private void setupUI(View view) {
        edtRewardTitle = view.findViewById(R.id.edtRewardTitle);
        edtRewardId = view.findViewById(R.id.edtRewardId);
        edtRewardCondition = view.findViewById(R.id.edtRewardCondition);
        edtRewardCate = view.findViewById(R.id.edtRewardCate);
        edtRewardPoint = view.findViewById(R.id.edtRewardPoint);
        edtDayAddReward = view.findViewById(R.id.edtDayAddReward);
        imgEditReward = view.findViewById(R.id.imgEditReward);

        edtDayAddReward.setOnClickListener(v -> showDatePickerDialog());

        Button btnDeleteReward = view.findViewById(R.id.btnDeleteReward);
        btnDeleteReward.setOnClickListener(v -> confirmDeletion());

        Button btnUpdateRewardInfo = view.findViewById(R.id.btnUpdateRewardInfo);
        btnUpdateRewardInfo.setOnClickListener(v -> saveChanges());

        Button btnPictureReward = view.findViewById(R.id.btnPictureReward);
        btnPictureReward.setOnClickListener(v -> imageHelper.openImagePicker(this));
    }

    private void populateData() {
        edtRewardId.setText(idReward);
        edtRewardTitle.setText(title);
        edtRewardCondition.setText(condition);
        edtRewardCate.setText(category);
        edtRewardPoint.setText(String.valueOf(point));
        edtDayAddReward.setText(dateUp);

        if (imageResource != null && !imageResource.isEmpty()) {
            imageHelper.loadImageFromRealtimeDatabase("Reward/" + idReward + "/imageResource", imgEditReward);
        }
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            edtDayAddReward.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveChanges() {
        String updatedTitle = edtRewardTitle.getText().toString().trim();
        String updatedCondition = edtRewardCondition.getText().toString().trim();
        String updatedCategory = edtRewardCate.getText().toString().trim();
        String updatedPointStr = edtRewardPoint.getText().toString().trim();
        String updatedDate = edtDayAddReward.getText().toString().trim();

        if (updatedTitle.isEmpty() || updatedCondition.isEmpty() || updatedPointStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tất cả các thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        int updatedPoint;
        try {
            updatedPoint = Integer.parseInt(updatedPointStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Điểm thưởng phải là số nguyên hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!updatedDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(getContext(), "Định dạng ngày không hợp lệ, vui lòng nhập theo dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("point", updatedPoint);
        updates.put("category", updatedCategory.isEmpty() ? "-1" : updatedCategory);
        updates.put("title", updatedTitle);
        updates.put("condition", updatedCondition);
        updates.put("imageResource", uploadedImageBase64 != null ? uploadedImageBase64 : imageResource);
        updates.put("dateUp", updatedDate);

        repository.update(idReward, updates, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "Cập nhật thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReward() {
        repository.delete(idReward, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "Xoá thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeletion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa ưu đãi")
                .setMessage("Bạn có chắc chắn muốn xóa ưu đãi này?")
                .setPositiveButton("Có", (dialog, which) -> deleteReward())
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FirebaseImageHelper.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageHelper.handleImageResult(requestCode, resultCode, data);

            if (imageHelper.getSelectedImageUri() != null) {
                imgEditReward.setImageURI(imageHelper.getSelectedImageUri());

                imageHelper.uploadImageToRealtimeDatabase(null, new FirebaseImageHelper.OnImageUploadListener() {
                    @Override
                    public void onSuccess(String base64Image) {
                        Toast.makeText(getContext(), "Ảnh đã được chọn thành công!", Toast.LENGTH_SHORT).show();
                        uploadedImageBase64 = base64Image;
                    }

                    @Override
                    public void onFailure(String message) {
                        uploadedImageBase64 = null;
                        Toast.makeText(getContext(), "Lỗi tải ảnh: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Không thể chọn ảnh. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
