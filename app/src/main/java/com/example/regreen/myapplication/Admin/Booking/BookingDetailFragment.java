package com.example.regreen.myapplication.Admin.Booking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.Booking;
import com.example.regreen.myapplication.MyAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDetailFragment extends BottomSheetDialogFragment {

    private String bookingId, cusName, cusEmail, cusPhone, dateBook, dateAppointment, address, status, timeAppointment;
    private List<String> categories, images;
    private FirebaseRepository<Booking> repository;
    private FirebaseImageHelper imageHelper;

    private EditText edtBookingId, edtCusName, edtCusEmail, edtCusPhone, edtDateBook, edtDateAppointment, edtAddressCollect;
    private AutoCompleteTextView dropdownStatus;
    private RecyclerView rvCategories, rvUserBookingListImg;

    public BookingDetailFragment() {}

    public static BookingDetailFragment newInstance(String bookingId, String cusName, String cusEmail, String cusPhone,
                                                    String dateBook, String dateAppointment, String timeAppointment,
                                                    String address, List<String> categories, List<String> images, String status) {
        BookingDetailFragment fragment = new BookingDetailFragment();
        Bundle args = new Bundle();
        args.putString("bookingId", bookingId);
        args.putString("cusName", cusName);
        args.putString("cusEmail", cusEmail);
        args.putString("cusPhone", cusPhone);
        args.putString("dateBook", dateBook);
        args.putString("dateAppointment", dateAppointment);
        args.putString("timeAppointment", timeAppointment); // Thêm tham số
        args.putString("address", address);
        args.putStringArrayList("categories", (ArrayList<String>) categories);
        args.putStringArrayList("images", (ArrayList<String>) images);
        args.putString("status", status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingId = getArguments().getString("bookingId");
            cusName = getArguments().getString("cusName");
            cusEmail = getArguments().getString("cusEmail");
            cusPhone = getArguments().getString("cusPhone");
            dateBook = getArguments().getString("dateBook");
            dateAppointment = getArguments().getString("dateAppointment");
            address = getArguments().getString("address");
            categories = getArguments().getStringArrayList("categories");
            images = getArguments().getStringArrayList("images");
            status = getArguments().getString("status");
            timeAppointment = getArguments().getString("timeAppointment"); // Đọc tham số mới
        }
        repository = new FirebaseRepository<>("Booking", Booking.class);
        imageHelper = new FirebaseImageHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_detail, container, false);
        setupUI(view);
        populateData();
        return view;
    }

    private void setupUI(View view) {
        edtBookingId = view.findViewById(R.id.edtBookingId);
        edtCusName = view.findViewById(R.id.edtCusName);
        edtCusEmail = view.findViewById(R.id.edtCusEmail);
        edtCusPhone = view.findViewById(R.id.edtCusPhone);
        edtDateBook = view.findViewById(R.id.edtDateBook);
        edtDateAppointment = view.findViewById(R.id.edtDateAppointment);
        edtAddressCollect = view.findViewById(R.id.edtAddressCollect);
        dropdownStatus = view.findViewById(R.id.edtStatusBooking);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvUserBookingListImg = view.findViewById(R.id.rvUserBookingListImg);

        Button btnUpdateBookingInfo = view.findViewById(R.id.btnUpdateBookingInfo);
        btnUpdateBookingInfo.setOnClickListener(v -> updateBookingStatus());

        Button btnDeleteBooking = view.findViewById(R.id.btnDeleteBooking);
        btnDeleteBooking.setOnClickListener(v -> confirmDeletion());

        setupStatusDropdown();
        setupRecyclerView();
    }

    private void setupStatusDropdown() {
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Đang xử lý");
        statusOptions.add("Chấp nhận yêu cầu");
        statusOptions.add("Đang trên đường đến");
        statusOptions.add("Hoàn thành");
        statusOptions.add("Đã hủy");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions);
        dropdownStatus.setAdapter(adapter);

        dropdownStatus.setOnClickListener(v -> dropdownStatus.showDropDown());
    }

    private void setupRecyclerView() {
        rvUserBookingListImg.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void populateData() {
        edtBookingId.setText(bookingId);
        edtCusName.setText(cusName);
        edtCusEmail.setText(cusEmail);
        edtCusPhone.setText(cusPhone);
        edtDateBook.setText(dateBook);
        edtAddressCollect.setText(address);

        dropdownStatus.setText(status, false);

        // Format ngày và giờ cho edtDateAppointment
        if (dateAppointment != null && timeAppointment != null && !dateAppointment.isEmpty() && !timeAppointment.isEmpty()) {
            edtDateAppointment.setText(dateAppointment + " " + timeAppointment); // Kết hợp ngày và giờ
        } else if (dateAppointment != null && !dateAppointment.isEmpty()) {
            edtDateAppointment.setText(dateAppointment); // Chỉ có ngày
        } else {
            edtDateAppointment.setText("Chưa có thông tin ngày hẹn"); // Giá trị mặc định
        }

        // Hiển thị danh sách categories
        if (categories != null && !categories.isEmpty()) {
            MyAdapter<String> categoryAdapter = new MyAdapter<>(categories, R.layout.layout_item_collect, (itemView, item) -> {
                TextView tvCateCollect = itemView.findViewById(R.id.tvCateCollect);
                tvCateCollect.setText(item);
            });
            rvCategories.setAdapter(categoryAdapter);
        } else {
            Toast.makeText(requireContext(), "Không có loại thu gom!", Toast.LENGTH_SHORT).show();
        }

        // Hiển thị danh sách hình ảnh
        if (images != null && !images.isEmpty()) {
            MyAdapter<String> imageAdapter = new MyAdapter<>(images, R.layout.layout_item_img_collect, (itemView, base64Image) -> {
                ImageView imgViewItem = itemView.findViewById(R.id.imgViewItem);
                displayBase64Image(base64Image, imgViewItem);
            });
            rvUserBookingListImg.setAdapter(imageAdapter);
        } else {
            Toast.makeText(requireContext(), "Không có hình ảnh!", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("BookingDetailFragment", "Lỗi giải mã Base64: " + e.getMessage());
                imageView.setImageResource(R.drawable.img_news_rectangle1); // Ảnh mặc định nếu lỗi
            }
        } else {
            imageView.setImageResource(R.drawable.img_news_rectangle1); // Ảnh mặc định nếu không có dữ liệu
        }
    }

    private void updateBookingStatus() {
        String updatedStatus = dropdownStatus.getText().toString().trim();
        if (updatedStatus.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn trạng thái!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", updatedStatus);

        repository.update(bookingId, updates, new FirebaseRepository.OnOperationListener() {
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

    private void deleteBooking() {
        repository.delete(bookingId, new FirebaseRepository.OnOperationListener() {
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
                .setTitle("Xóa Booking")
                .setMessage("Bạn có chắc chắn muốn xóa lịch hẹn này?")
                .setPositiveButton("Có", (dialog, which) -> deleteBooking())
                .setNegativeButton("Không", null)
                .show();
    }
}
