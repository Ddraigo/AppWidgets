package com.example.regreen.myapplication.Admin.Booking;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.Booking;
import com.example.regreen.myapplication.ModelData.User;
import com.example.regreen.myapplication.MyAdapter;

import java.util.HashMap;
import java.util.List;

public class BookingManagerActivity extends AppCompatActivity {

    private RecyclerView rvUserBookingList;
    private MyAdapter<Booking> adapter;
    private TextView tv_empty_message2;
    private ImageButton btnBackManageBooking;
    private HashMap<String, User> userCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_manager);

        getWindow().setStatusBarColor(getResources().getColor(R.color.green_bg, null));

        rvUserBookingList = findViewById(R.id.rvUserBookingList);
        rvUserBookingList.setLayoutManager(new LinearLayoutManager(this));
        tv_empty_message2 = findViewById(R.id.tv_empty_message2);
        btnBackManageBooking = findViewById(R.id.btnBackManageBooking);

        btnBackManageBooking.setOnClickListener(v -> finish());

        loadBookings();
    }

    public void loadBookings() {
        FirebaseRepository<Booking> bookingRepository = new FirebaseRepository<>("Booking", Booking.class);

        bookingRepository.getAllRealTime(new FirebaseRepository.OnFetchListListener<Booking>() {
            @Override
            public void onSuccess(List<Booking> bookingList) {
                Log.d("BookingManagerActivity", "Dữ liệu Booking đã được tải. Tổng số: " + bookingList.size());
                if (bookingList.isEmpty()) {
                    tv_empty_message2.setVisibility(View.VISIBLE);
                    rvUserBookingList.setVisibility(View.GONE);
                    return; // Dừng lại nếu không có dữ liệu Booking
                }

                tv_empty_message2.setVisibility(View.GONE);
                rvUserBookingList.setVisibility(View.VISIBLE);

                // Duyệt qua từng Booking để lấy thông tin User
                int[] remainingUsers = {bookingList.size()}; // Số lượng User cần tải
                for (Booking booking : bookingList) {
                    String email = booking.getEmail();
                    if (email == null || email.isEmpty()) {
                        Log.e("BookingManagerActivity", "Booking không có email: " + booking.getIdBooking());
                        remainingUsers[0]--;
                        continue; // Bỏ qua Booking không có email
                    }

                    String emailKey = email.replace(".", ",");

                    // Chỉ tải User nếu chưa có trong cache
                    if (!userCache.containsKey(emailKey)) {
                        FirebaseRepository<User> userRepository = new FirebaseRepository<>("User", User.class);
                        userRepository.get(emailKey, new FirebaseRepository.OnFetchListener<User>() {
                            @Override
                            public void onSuccess(User user) {
                                if (user != null) {
                                    userCache.put(emailKey, user); // Lưu vào cache
                                }
                                remainingUsers[0]--;
                                if (remainingUsers[0] == 0) {
                                    // Khi tất cả thông tin đã được tải
                                    setupRecyclerView(bookingList);
                                }
                            }

                            @Override
                            public void onFailure(String message) {
                                Log.e("BookingManagerActivity", "Lỗi khi tải User: " + message);
                                remainingUsers[0]--;
                                if (remainingUsers[0] == 0) {
                                    setupRecyclerView(bookingList);
                                }
                            }
                        });
                    } else {
                        remainingUsers[0]--;
                        if (remainingUsers[0] == 0) {
                            setupRecyclerView(bookingList);
                        }
                    }
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("BookingManagerActivity", "Lỗi khi tải Booking: " + message);
                Toast.makeText(BookingManagerActivity.this, "Không thể tải dữ liệu Booking", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupRecyclerView(List<Booking> bookingList) {
        if (bookingList == null || bookingList.isEmpty()) {
            Log.d("BookingManagerActivity", "Danh sách Booking trống.");
            return;
        }

        if (adapter == null) {
            adapter = new MyAdapter<>(bookingList, R.layout.add_item_layout, (itemView, booking) -> {
                // Liên kết các View từ layout
                TextView titleTextView = itemView.findViewById(R.id.titleItem);
                TextView idTextView = itemView.findViewById(R.id.idItem);
                TextView dateTextView = itemView.findViewById(R.id.dayAddItem);
                ImageView imgItem = itemView.findViewById(R.id.imgItem);
                ImageView imgStatusOn = itemView.findViewById(R.id.imgStatusOn);

                // Hiển thị thông tin Booking
                idTextView.setText("Trạng thái: " + booking.getStatus());
                dateTextView.setText("Ngày tạo: " + booking.getDateBook());
                imgItem.setImageResource(R.drawable.img_news_rectangle1);

                // Hiển thị trạng thái
                switch (booking.getStatus()) {
                    case "Đã hủy":
                        imgStatusOn.setImageResource(R.drawable.img_status_offline);
                        break;
                    case "Hoàn thành":
                        imgStatusOn.setImageResource(R.drawable.img_status_online);
                        break;
                    case "Đang trên đường đến":
                    imgStatusOn.setImageResource(R.drawable.img_status_blue);
                    break;
                    default:
                        imgStatusOn.setImageResource(R.drawable.img_status_orange);
                        break;
                }

                // Lấy thông tin User từ cache
                User user = userCache.get(booking.getEmail().replace(".", ","));
                if (user != null) {
                    titleTextView.setText("Họ tên: " + user.getName());
                } else {
                    titleTextView.setText("Tên: Đang tải...");
                }

                // Xử lý sự kiện nhấp vào mục
                itemView.setOnClickListener(v -> openBookingDetailFragment(booking));
            });

            rvUserBookingList.setAdapter(adapter);
        } else {
            adapter.updateData(bookingList);
        }
    }

    private void openBookingDetailFragment(Booking booking) {
        User user = userCache.get(booking.getEmail().replace(".", ","));
        if (user != null) {
            BookingDetailFragment bookingDetailFragment = BookingDetailFragment.newInstance(
                    booking.getIdBooking(),
                    user.getName(),
                    user.getEmail(),
                    user.getNumberPhone(),
                    booking.getDateBook(),
                    booking.getDateAppointment(),
                    booking.getTimeAppointment(),
                    booking.getAddress(),
                    booking.getListCategory(),
                    booking.getListImage(),
                    booking.getStatus()
            );
            bookingDetailFragment.show(getSupportFragmentManager(), "BookingDetailFragment");
        } else {
            Toast.makeText(this, "Không thể lấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
        }
    }
}
