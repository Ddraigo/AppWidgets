package com.example.regreen.myapplication.User;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.Login;
import com.example.regreen.myapplication.ModelData.AccessPoint;
import com.example.regreen.myapplication.ModelData.Booking;
import com.example.regreen.myapplication.ModelData.User;
import com.example.regreen.myapplication.MyAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserBooking extends AppCompatActivity {

    private TextView tv_Date, tv_Time, inputCollectionPoint;
    private RecyclerView recyclerViewCollectionPoints, rvAddImageCollection;
    private Button btnSaveBooking, btnShowDatePicker, btnShowTimePicker;
    private ImageButton btnBackUserBooking;

    private List<String> selectedWasteTypes = new ArrayList<>();
    private List<String> uploadedImages = new ArrayList<>();
    private List<String> cardImages;
    private AccessPoint selectedCollectionPoint;

    private FirebaseRepository<AccessPoint> accessPointRepository;
    private FirebaseRepository<Booking> bookingRepository;
    private FirebaseRepository<User> userRepository;

    private MyAdapter<AccessPoint> collectionPointAdapter;
    private MyAdapter<String> cardAdapter;
    private FirebaseImageHelper imageHelper;

    private int selectedImagePosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        SharedPreferences sharedPreferences = UserBooking.this.getSharedPreferences(Login.PREF_NAME, Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "Guest");

        String userEmail = getIntent().getStringExtra("userEmail");
        if (userEmail == null && userName == null) {
            Toast.makeText(this, "Không thể xác định người dùng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRepository = new FirebaseRepository<>("User", User.class);

        recyclerViewCollectionPoints = findViewById(R.id.recyclerViewCollectionPoints);
        rvAddImageCollection = findViewById(R.id.rvAddImageCollection);
        inputCollectionPoint = findViewById(R.id.inputColectionPoint);
        btnSaveBooking = findViewById(R.id.btnSaveBooking);
        tv_Date = findViewById(R.id.tv_date);
        tv_Time = findViewById(R.id.tv_time);
        btnShowDatePicker = findViewById(R.id.btnShowDatePicker);
        btnShowTimePicker = findViewById(R.id.btnShowTimePicker);
        btnBackUserBooking = findViewById(R.id.btnBackUserBooking);

        recyclerViewCollectionPoints.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Firebase setup
        accessPointRepository = new FirebaseRepository<>("AccessPoint", AccessPoint.class);
        bookingRepository = new FirebaseRepository<>("Booking", Booking.class);
        imageHelper = new FirebaseImageHelper(this);

        btnShowDatePicker.setOnClickListener(v -> openDialog());
        btnShowTimePicker.setOnClickListener(v -> openTimeDialog());
        btnBackUserBooking.setOnClickListener(v -> finish());

        inputCollectionPoint.setOnClickListener(v -> showAddressDialog());
        btnSaveBooking.setOnClickListener(v -> {
            if (validateBooking()) {
                saveBooking(userEmail);
            }
        });

        setupRecyclerViewForImages();
        loadCollectionPoints();
        setupWasteTypeListeners();
    }

    private void setupRecyclerViewForImages() {
        cardImages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cardImages.add(""); // Initialize with empty strings (no images yet)
        }

        cardAdapter = new MyAdapter<>(cardImages, R.layout.layout_card_add_image, (itemView, imageUrl) -> {
            ImageView cardImagesCollect = itemView.findViewById(R.id.cardImagesCollect);

            if (imageUrl.isEmpty()) {
                cardImagesCollect.setImageResource(R.drawable.image_add_img);
            } else {
                displayBase64Image(imageUrl, cardImagesCollect);
            }

            itemView.setOnClickListener(v -> {
                int position = rvAddImageCollection.getChildAdapterPosition(itemView);
                imageHelper.openImagePicker(UserBooking.this);
                selectedImagePosition = position;
            });
        });

        rvAddImageCollection.setAdapter(cardAdapter);
        rvAddImageCollection.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageHelper.handleImageResult(requestCode, resultCode, data);

        if (imageHelper.getSelectedImageUri() != null) {
            imageHelper.uploadImageToRealtimeDatabase(null, new FirebaseImageHelper.OnImageUploadListener() {
                @Override
                public void onSuccess(String base64Image) {
                    if (selectedImagePosition != RecyclerView.NO_POSITION) {
                        // Cập nhật danh sách ảnh để lưu lên Firebase
                        uploadedImages.add(base64Image);

                        // Hiển thị lại ảnh trong giao diện RecyclerView
                        cardImages.set(selectedImagePosition, base64Image);
                        cardAdapter.notifyItemChanged(selectedImagePosition);
                    }
                    Toast.makeText(UserBooking.this, "Tải ảnh thành công!", Toast.LENGTH_SHORT).show();
                }


                @Override
                public void onFailure(String message) {
                    Toast.makeText(UserBooking.this, "Tải ảnh thất bại: " + message, Toast.LENGTH_SHORT).show();
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
                imageView.setImageResource(R.drawable.image_add_img);
            }
        } else {
            imageView.setImageResource(R.drawable.image_add_img);
        }
    }

    private void loadCollectionPoints() {
        accessPointRepository.getAll(new FirebaseRepository.OnFetchListListener<AccessPoint>() {
            @Override
            public void onSuccess(List<AccessPoint> accessPoints) {
                List<AccessPoint> collectionPoints = new ArrayList<>();
                for (AccessPoint point : accessPoints) {
                    if ("CollectionPoints".equals(point.getCategory())) {
                        collectionPoints.add(point);
                    }
                }

                if (collectionPoints.isEmpty()) {
                    Toast.makeText(UserBooking.this, "Không tìm thấy điểm thu gom", Toast.LENGTH_SHORT).show();
                } else {
                    setupRecyclerViewForCollectionPoints(collectionPoints);
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(UserBooking.this, "Tải dữ liệu thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerViewForCollectionPoints(List<AccessPoint> collectionPoints) {
        final int[] selectedPosition = {RecyclerView.NO_POSITION};

        collectionPointAdapter = new MyAdapter<>(
                collectionPoints,
                R.layout.layout_access_point,
                (itemView, item) -> {
                    TextView textCard = itemView.findViewById(R.id.textCard);
                    textCard.setText(item.getReceivingPoint());

                    boolean isSelected = selectedPosition[0] == collectionPoints.indexOf(item);
                    itemView.setSelected(isSelected);

                    itemView.setOnClickListener(v -> {
                        int currentPosition = collectionPoints.indexOf(item);

                        int previousPosition = selectedPosition[0];
                        selectedPosition[0] = currentPosition;

                        if (previousPosition != RecyclerView.NO_POSITION) {
                            collectionPointAdapter.notifyItemChanged(previousPosition);
                        }
                        collectionPointAdapter.notifyItemChanged(currentPosition);

                        selectedCollectionPoint = item;
                        Toast.makeText(UserBooking.this, "Đã chọn " + item.getReceivingPoint(), Toast.LENGTH_SHORT).show();
                    });
                }
        );

        recyclerViewCollectionPoints.setAdapter(collectionPointAdapter);
    }

    private void setupWasteTypeListeners() {
        int[] cardIds = {R.id.cardPaper, R.id.cardGlass, R.id.cardPlastic, R.id.cardPin, R.id.CardNylonBag, R.id.CardMetal};
        String[] wasteTypes = {"Giấy", "Thủy tinh", "Nhựa", "Pin", "Túi Nylon", "Kim loại"};

        for (int i = 0; i < cardIds.length; i++) {
            View card = findViewById(cardIds[i]);
            String wasteType = wasteTypes[i];
            if (card != null) {
                card.setOnClickListener(v -> toggleWasteType(wasteType, card));
            }
        }
    }

    private void toggleWasteType(String type, View cardView) {
        if (selectedWasteTypes.contains(type)) {
            selectedWasteTypes.remove(type); // Nếu đã có, thì xóa khỏi danh sách
        } else {
            selectedWasteTypes.add(type); // Nếu chưa có, thì thêm vào danh sách
        }
        cardView.setSelected(!cardView.isSelected()); // Cập nhật trạng thái giao diện
    }

    private boolean validateBooking() {
        if (selectedCollectionPoint == null) {
            Toast.makeText(this, "Vui lòng chọn một điểm thu gom!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (inputCollectionPoint.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ cụ thể!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedWasteTypes.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một loại rác!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveBooking(String userEmail) {
        String address = inputCollectionPoint.getText().toString().trim();
        String dateBook = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String dateAppointment = tv_Date.getText().toString().trim();
        String timeAppointment = tv_Time.getText().toString().trim();

        if (userEmail == null) {
            Toast.makeText(this, "Không thể xác định người dùng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String bookingId = "booking_" + System.currentTimeMillis();
        Booking booking = new Booking(
                bookingId,
                userEmail,
                new ArrayList<>(selectedWasteTypes),
                dateBook,
                dateAppointment,
                timeAppointment,
                address,
                new ArrayList<>(uploadedImages),
                "Đang xử lý"
        );

        bookingRepository.save(bookingId, booking, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(UserBooking.this, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại màn hình trước
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(UserBooking.this, "Đặt lịch thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            tv_Date.setText(String.format("%02d/%02d/%04d", selectedDay, (selectedMonth + 1), selectedYear));
        }, year, month, day);

        dialog.show();
    }

    private void openTimeDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timeDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String amPm = selectedHour >= 12 ? "PM" : "AM";
                    int hourIn12Format = selectedHour > 12 ? selectedHour - 12 : selectedHour;
                    if (hourIn12Format == 0) {
                        hourIn12Format = 12;
                    }
                    tv_Time.setText(String.format("%02d:%02d %s", hourIn12Format, selectedMinute, amPm));
                }, hour, minute, false);

        timeDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialog, which) -> {

        });

        timeDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hủy", (dialog, which) -> {
            dialog.dismiss();
        });

        timeDialog.show();
    }


    private void showAddressDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_address, null);
        EditText edtAddressPoint = dialogView.findViewById(R.id.edtAddressPoint);
        Button btnAddAddress = dialogView.findViewById(R.id.btnAddAdress);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        String currentAddress = inputCollectionPoint.getText().toString().trim();
        if (!currentAddress.isEmpty()) {
            edtAddressPoint.setText(currentAddress);
        }

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnAddAddress.setOnClickListener(v -> {
            String address = edtAddressPoint.getText().toString().trim();
            if (!address.isEmpty()) {
                inputCollectionPoint.setText(address);
                dialog.dismiss();
            } else {
                Toast.makeText(UserBooking.this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


}
