package com.example.regreen.myapplication.Admin.Recycle;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.RecycleItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;

public class RecycleDetailFragment extends BottomSheetDialogFragment {

    private String idRecycle, recycleName, recycleDetail, imageResource;
    private FirebaseRepository<RecycleItem> repository;
    private FirebaseImageHelper imageHelper;
    private EditText edtRecycleName, edtRecycleDetail;
    private Spinner edtRecycleCategory, edtIsRecycle;
    private ImageView imgRecyclePreview;
    private String uploadedImageBase64;

    private static final String[] CATEGORY_OPTIONS = {"Nhựa", "Giấy", "Kim loại", "Thủy tinh", "Rác thải điện tử", "Thực phẩm", "Khác"};
    private static final String[] RECYCLE_OPTIONS = {"Tái chế", "Không tái chế"};

    public static RecycleDetailFragment newInstance(String idRecycle, int category, int isRecycle, String recycleName, String recycleDetail, String imageResource) {
        RecycleDetailFragment fragment = new RecycleDetailFragment();
        Bundle args = new Bundle();
        args.putString("idRecycle", idRecycle);
        args.putInt("category", category);
        args.putInt("isRecycle", isRecycle);
        args.putString("recycleName", recycleName);
        args.putString("recycleDetail", recycleDetail);
        args.putString("imageResource", imageResource);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idRecycle = getArguments().getString("idRecycle");
            recycleName = getArguments().getString("recycleName");
            recycleDetail = getArguments().getString("recycleDetail");
            imageResource = getArguments().getString("imageResource");
            repository = new FirebaseRepository<>("Recycles", RecycleItem.class);
            imageHelper = new FirebaseImageHelper(requireContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycle_detail, container, false);
        setupUI(view);
        populateData();
        return view;
    }

    private void setupUI(View view) {
        edtRecycleCategory = view.findViewById(R.id.edtRecycleCategory);
        edtRecycleName = view.findViewById(R.id.edtRecycleName);
        edtRecycleDetail = view.findViewById(R.id.edtRecycleDetail);
        imgRecyclePreview = view.findViewById(R.id.imgEditRecycle);
        edtIsRecycle = view.findViewById(R.id.edtIsRecycle);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, CATEGORY_OPTIONS);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edtRecycleCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> recycleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, RECYCLE_OPTIONS);
        recycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edtIsRecycle.setAdapter(recycleAdapter);

        Button btnDeleteRecycle = view.findViewById(R.id.btnDeleteRecycle);
        btnDeleteRecycle.setOnClickListener(v -> confirmDeletion());

        Button btnUpdateRecycleInfo = view.findViewById(R.id.btnEditRecycle);
        btnUpdateRecycleInfo.setOnClickListener(v -> saveChanges());

        Button btnPictureRecycle = view.findViewById(R.id.btnEditPictureRecycle);
        btnPictureRecycle.setOnClickListener(v -> imageHelper.openImagePicker(this));
    }

    private void populateData() {
        edtRecycleName.setText(recycleName);
        edtRecycleDetail.setText(recycleDetail);

        if (getArguments() != null) {
            int categoryIndex = getArguments().getInt("category");
            edtRecycleCategory.setSelection(categoryIndex >= 0 && categoryIndex < CATEGORY_OPTIONS.length ? categoryIndex : 0);

            int isRecycleIndex = getArguments().getInt("isRecycle") == 1 ? 0 : 1;
            edtIsRecycle.setSelection(isRecycleIndex);
        }

        if (imageResource != null && !imageResource.isEmpty()) {
            imageHelper.loadImageFromRealtimeDatabase("Recycles/" + idRecycle + "/imageResource", imgRecyclePreview);
        } else {
            Toast.makeText(getContext(), "Không có ảnh từ Firebase, sử dụng ảnh mặc định.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges() {
        String updatedCategory = edtRecycleCategory.getSelectedItem().toString().trim();
        String updatedRecycleOption = edtIsRecycle.getSelectedItem().toString().trim();

        int updatedIsRecycle = updatedRecycleOption.equals("Tái chế") ? 1 : 0;
        String updatedRecycleName = edtRecycleName.getText().toString().trim();
        String updatedRecycleDetail = edtRecycleDetail.getText().toString().trim();

        if (updatedRecycleName.isEmpty() || updatedRecycleDetail.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tất cả các thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        int updatedCategoryIndex = getCategoryIndex(updatedCategory);
        if (updatedCategoryIndex == -1) {
            Toast.makeText(getContext(), "Danh mục không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("category", updatedCategoryIndex);
        updates.put("recycleName", updatedRecycleName);
        updates.put("recycleDetail", updatedRecycleDetail);
        updates.put("isRecycle", updatedIsRecycle);
        updates.put("imageResource", uploadedImageBase64 != null ? uploadedImageBase64 : imageResource);

        repository.update(idRecycle, updates, new FirebaseRepository.OnOperationListener() {
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

    private int getCategoryIndex(String category) {
        for (int i = 0; i < CATEGORY_OPTIONS.length; i++) {
            if (CATEGORY_OPTIONS[i].equals(category)) {
                return i;
            }
        }
        return -1;
    }

    private void deleteRecycle() {
        repository.delete(idRecycle, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "Xóa thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeletion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa phân loại")
                .setMessage("Bạn có chắc chắn muốn xóa phân loại này?")
                .setPositiveButton("Có", (dialog, which) -> deleteRecycle())
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FirebaseImageHelper.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageHelper.handleImageResult(requestCode, resultCode, data);

            if (imageHelper.getSelectedImageUri() != null) {
                imgRecyclePreview.setImageURI(imageHelper.getSelectedImageUri());

                imageHelper.uploadImageToRealtimeDatabase(null, new FirebaseImageHelper.OnImageUploadListener() {
                    @Override
                    public void onSuccess(String base64Image) {
                        uploadedImageBase64 = base64Image;
                        Toast.makeText(getContext(), "Ảnh đã được chọn thành công!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        // Reset biến uploadedImageBase64 nếu tải ảnh thất bại
                        uploadedImageBase64 = null;
                        Toast.makeText(getContext(), "Tải ảnh thất bại: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}

