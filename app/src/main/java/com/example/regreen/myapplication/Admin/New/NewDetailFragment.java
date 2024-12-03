package com.example.regreen.myapplication.Admin.New;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.example.regreen.myapplication.ModelData.News;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewDetailFragment extends BottomSheetDialogFragment {

    private String idNew, title, content, imageResource, category, dateUp;
    private final Calendar calendar = Calendar.getInstance();
    private FirebaseRepository<News> repository;
    private FirebaseImageHelper imageHelper;
    private EditText edtTitle, edtId, edtContent, edtCategory, edtDate;
    private ImageView imgEditPreview;
    private String uploadedImageBase64;

    public NewDetailFragment() {}

    public static NewDetailFragment newInstance(String idNew, String title, String content, String imageResource, String category, String dateUp) {
        NewDetailFragment fragment = new NewDetailFragment();
        Bundle args = new Bundle();
        args.putString("idNew", idNew);
        args.putString("title", title);
        args.putString("content", content);
        args.putString("imageResource", imageResource);
        args.putString("category", category);
        args.putString("dateUp", dateUp);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idNew = getArguments().getString("idNew");
            title = getArguments().getString("title");
            content = getArguments().getString("content");
            imageResource = getArguments().getString("imageResource");
            category = getArguments().getString("category");
            dateUp = getArguments().getString("dateUp");
        }

        repository = new FirebaseRepository<>("News", News.class);
        imageHelper = new FirebaseImageHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_detail, container, false);
        setupUI(view);
        populateData();
        return view;
    }

    private void setupUI(View view) {
        edtTitle = view.findViewById(R.id.edtNewTitle);
        edtId = view.findViewById(R.id.edtNewId);
        edtContent = view.findViewById(R.id.edtNewDetail);
        edtCategory = view.findViewById(R.id.edtNewCate);
        edtDate = view.findViewById(R.id.edtDayAdd);
        imgEditPreview = view.findViewById(R.id.imgEditPreview);

        edtDate.setOnClickListener(v -> showDatePickerDialog());

        Button btnDeleteNew = view.findViewById(R.id.btnDeleteNew);
        btnDeleteNew.setOnClickListener(v -> confirmDeletion());

        Button btnUpdateNewInfo = view.findViewById(R.id.btnUpdateNewInfo);
        btnUpdateNewInfo.setOnClickListener(v -> saveChanges());

        Button btnPictureNew = view.findViewById(R.id.btnPictureNew);
        btnPictureNew.setOnClickListener(v -> imageHelper.openImagePicker(this));
    }

    private void populateData() {
        edtId.setText(idNew);
        edtTitle.setText(title);
        edtContent.setText(content);
        edtCategory.setText(category);
        edtDate.setText(dateUp);

        if (imageResource != null && !imageResource.isEmpty()) {
            imageHelper.loadImageFromRealtimeDatabase("News/" + idNew + "/imageResource", imgEditPreview);
        }
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            edtDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveChanges() {
        String updatedTitle = edtTitle.getText().toString().trim();
        String updatedContent = edtContent.getText().toString().trim();
        String updatedCategory = edtCategory.getText().toString().trim();
        String updatedDate = edtDate.getText().toString().trim();

        if (updatedTitle.isEmpty() || updatedContent.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tất cả các thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!updatedDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(getContext(), "Định dạng ngày không hợp lệ, vui lòng nhập theo dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", updatedTitle);
        updates.put("content", updatedContent);
        updates.put("category", updatedCategory.isEmpty() ? "-1" : updatedCategory);
        updates.put("imageResource", uploadedImageBase64 != null ? uploadedImageBase64 : imageResource); // Use new image if available
        updates.put("dateUp", updatedDate);

        repository.update(idNew, updates, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().setFragmentResult("newsUpdated", new Bundle());
                }
                dismiss();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "Cập nhật thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteNews() {
        repository.delete(idNew, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().setFragmentResult("newsDeleted", new Bundle());
                }
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
                .setTitle("Xóa tin tức")
                .setMessage("Bạn có chắc chắn muốn xóa tin tức này?")
                .setPositiveButton("Có", (dialog, which) -> deleteNews())
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FirebaseImageHelper.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageHelper.handleImageResult(requestCode, resultCode, data);

            if (imageHelper.getSelectedImageUri() != null) {
                imgEditPreview.setImageURI(imageHelper.getSelectedImageUri()); // Display selected image

                // Convert image to Base64 and store for later save
                imageHelper.uploadImageToRealtimeDatabase(null, new FirebaseImageHelper.OnImageUploadListener() {
                    @Override
                    public void onSuccess(String base64Image) {
                        Toast.makeText(getContext(), "Ảnh đã được chọn thành công!", Toast.LENGTH_SHORT).show();
                        uploadedImageBase64 = base64Image; // Store Base64 string
                    }

                    @Override
                    public void onFailure(String message) {
                        uploadedImageBase64 = null; // Reset if upload fails
                        Toast.makeText(getContext(), "Lỗi tải ảnh: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Không thể chọn ảnh. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

