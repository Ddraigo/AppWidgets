package com.example.regreen.myapplication.Admin.Recycle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.RecycleItem; // Changed to RecycleItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddRecycleFragment extends BottomSheetDialogFragment {

    private EditText edtAddRecycleName, edtAddRecycleDescription;
    private Button btnSaveRecycle, btnAddImageRecycle;
    private ImageView imgPreview;
    private String imageUrl;
    private FirebaseImageHelper imageHelper;
    private Spinner spinnerAddRecycleCategory, spinnerAddIsRecycle;
    private String[] categoryOptions = {"Nhựa", "Giấy", "Kim loại", "Thủy tinh", "Rác thải điện tử", "Thực phẩm", "Khác"};
    private String[] recycleOptions = {"Tái chế", "Không tái chế"};

    public AddRecycleFragment() {}

    public static AddRecycleFragment newInstance() {
        return new AddRecycleFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_recycle, container, false);

        // Initialize views
        edtAddRecycleName = view.findViewById(R.id.edtAddRecycleName);
        edtAddRecycleDescription = view.findViewById(R.id.edtAddRecycleDetail);
        imgPreview = view.findViewById(R.id.imgAddRecycle);
        btnAddImageRecycle = view.findViewById(R.id.btnAddPictureRecycle);
        btnSaveRecycle = view.findViewById(R.id.btnSaveRecycle);
        spinnerAddRecycleCategory = view.findViewById(R.id.spinnerAddRecycleCategory);
        spinnerAddIsRecycle = view.findViewById(R.id.edtAddIsRecycle);

        // Initialize the FirebaseImageHelper
        imageHelper = new FirebaseImageHelper(requireContext());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddRecycleCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> recycleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, recycleOptions);
        recycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAddIsRecycle.setAdapter(recycleAdapter);

        btnAddImageRecycle.setOnClickListener(v -> imageHelper.openImagePicker(this));

        btnSaveRecycle.setOnClickListener(v -> saveRecycleDataToFirebase());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageHelper.handleImageResult(requestCode, resultCode, data);

        if (imageHelper.getSelectedImageUri() != null) {
            // Upload the image to Firebase
            imageHelper.uploadImageToRealtimeDatabase(null, new FirebaseImageHelper.OnImageUploadListener() {
                @Override
                public void onSuccess(String base64Image) {
                    imageUrl = base64Image;
                    displayBase64Image(base64Image, imgPreview);
                    Toast.makeText(requireContext(), "Tải ảnh thành công!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(requireContext(), "Tải ảnh thất bại: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "Không có ảnh nào được chọn", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRecycleDataToFirebase() {
        String name = edtAddRecycleName.getText().toString().trim();
        String description = edtAddRecycleDescription.getText().toString().trim();

        int selectedCategoryPosition = spinnerAddRecycleCategory.getSelectedItemPosition();
        int categoryId = selectedCategoryPosition == -1 ? -1 : selectedCategoryPosition;

        int isRecycle = spinnerAddIsRecycle.getSelectedItemPosition() == 0 ? 1 : 0;

        // Validate input fields
        if (name.isEmpty() || description.isEmpty() || categoryId == -1) {
            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure an image has been selected
        if (imageUrl == null || imageUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn một bức ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        String recycleId = "recycle_" + System.currentTimeMillis();
        RecycleItem recycleItem = new RecycleItem(recycleId, name, description, imageUrl, categoryId, isRecycle);

        FirebaseRepository<RecycleItem> repository = new FirebaseRepository<>("Recycles", RecycleItem.class);
        repository.save(recycleId, recycleItem, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(requireContext(), "Lưu thành công!", Toast.LENGTH_SHORT).show();
                clearFields();
                dismiss();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(requireContext(), "Lưu thất bại: " + message, Toast.LENGTH_SHORT).show();
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
                imageView.setImageResource(R.drawable._981712_scaled);
            }
        } else {
            imageView.setImageResource(R.drawable._981712_scaled);
        }
    }

    private void clearFields() {
        edtAddRecycleName.setText("");
        edtAddRecycleDescription.setText("");
        imgPreview.setImageResource(R.drawable._981712_scaled); // Default image
        imageUrl = null;
    }
}
