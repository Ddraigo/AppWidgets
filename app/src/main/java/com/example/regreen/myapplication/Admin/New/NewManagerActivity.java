package com.example.regreen.myapplication.Admin.New;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.News;
import com.example.regreen.myapplication.MyAdapter;

import java.util.List;

public class NewManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNewList;
    private MyAdapter<News> adapter;
    private TextView tvEmptyMessage;
    private ImageButton btnBackManageNew;
    private Button btnAddNew;
    private FirebaseImageHelper imageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_manager);

        getWindow().setStatusBarColor(getResources().getColor(R.color.green_bg, null));

        recyclerViewNewList = findViewById(R.id.recyclerViewNewList);
        recyclerViewNewList.setLayoutManager(new LinearLayoutManager(this));
        tvEmptyMessage = findViewById(R.id.tv_empty_message2);
        btnBackManageNew = findViewById(R.id.btnBackManageNew);
        btnAddNew = findViewById(R.id.btnAddNew);

        imageHelper = new FirebaseImageHelper(this);

        btnAddNew.setOnClickListener(v -> openAddNewFragment());
        btnBackManageNew.setOnClickListener(v -> finish());

        loadData();

        getSupportFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
            if (result.getBoolean("dataUpdated")) {
                loadData();
            }
        });

        getSupportFragmentManager().setFragmentResultListener("newsUpdated", this, (requestKey, result) -> loadData());
        getSupportFragmentManager().setFragmentResultListener("newsDeleted", this, (requestKey, result) -> loadData());
    }



    private void openAddNewFragment() {
        AddNewFragment addNewFragment = new AddNewFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, addNewFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadData() {
        FirebaseRepository<News> newsRepository = new FirebaseRepository<>("News", News.class);

        newsRepository.getAllRealTime(new FirebaseRepository.OnFetchListListener<News>() {
            @Override
            public void onSuccess(List<News> data) {
                Log.d("NewManagerActivity", "Dữ liệu đã được cập nhật. Tổng số: " + data.size());
                if (data.isEmpty()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    recyclerViewNewList.setVisibility(View.GONE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                    recyclerViewNewList.setVisibility(View.VISIBLE);
                    setupRecyclerView(data);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("NewManagerActivity", "Lỗi khi tải dữ liệu: " + message);
                Toast.makeText(NewManagerActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView(List<News> newsList) {
        if (adapter == null) {
            adapter = new MyAdapter<>(newsList, R.layout.add_item_layout, new MyAdapter.Binder<News>() {
                @Override
                public void bind(View itemView, News news) {
                    TextView titleTextView = itemView.findViewById(R.id.titleItem);
                    TextView contentTextView = itemView.findViewById(R.id.idItem);
                    TextView dateTextView = itemView.findViewById(R.id.dayAddItem);
                    ImageView imgItem = itemView.findViewById(R.id.imgItem);

                    titleTextView.setText("ID: " + news.getIdNew());
                    contentTextView.setText("Tiêu đề: " + news.getTitle());
                    dateTextView.setText("Ngày đăng: " + news.getDateUp());

                    // Hiển thị ảnh trực tiếp từ Base64
                    if (news.getImageResource() != null && !news.getImageResource().isEmpty()) {
                        displayBase64Image(news.getImageResource(), imgItem);
                    } else {
                        imgItem.setImageResource(R.drawable.img_news_rectangle1); // Ảnh mặc định
                    }

                    itemView.setOnClickListener(v -> openNewsDetailFragment(news));
                }
            });
            recyclerViewNewList.setAdapter(adapter);
        } else {
            adapter.updateData(newsList);
        }
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("NewManagerActivity", "Lỗi giải mã Base64: " + e.getMessage());
                imageView.setImageResource(R.drawable.img_news_rectangle1); // Ảnh mặc định nếu lỗi
            }
        } else {
            imageView.setImageResource(R.drawable.img_news_rectangle1); // Ảnh mặc định nếu không có dữ liệu
        }
    }

    private void openNewsDetailFragment(News news) {
        NewDetailFragment newDetailFragment = NewDetailFragment.newInstance(
                news.getIdNew(),
                news.getTitle(),
                news.getContent(),
                news.getImageResource(),
                news.getCategory(),
                news.getDateUp()
        );

        newDetailFragment.show(getSupportFragmentManager(), "NewDetailFragment");
    }
}
