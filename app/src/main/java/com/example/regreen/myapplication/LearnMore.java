package com.example.regreen.myapplication;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Fragment.RecycleDatailInfoFragment;
import com.example.regreen.myapplication.ModelData.RecycleItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LearnMore extends AppCompatActivity {

    private TextView tvRecycle, tvNonRecycle;
    private ImageButton btnBackLearnMore;
    private RecyclerView recyclerView;
    private MyAdapter<RecycleItem> adapter;
    private List<RecycleItem> allItems = new ArrayList<>();
    private int isRecycleItem = 1;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Recycles");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        tvRecycle = findViewById(R.id.tvRecycle);
        tvNonRecycle = findViewById(R.id.tvNonRecycle);
        recyclerView = findViewById(R.id.list_recycle_items);
        btnBackLearnMore = findViewById(R.id.btnBackLearnMore);
        Button btnHowToRecycle = findViewById(R.id.btnHowToRecycle);
        Button btnOpenLearnMore = findViewById(R.id.btnOpenLearnMore);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter<>(new ArrayList<>(), R.layout.recycle_items, (itemView, item) -> {
            TextView itemName = itemView.findViewById(R.id.textItem);
            ImageView itemImage = itemView.findViewById(R.id.imageItem);

            itemName.setText(item.getName());
            String base64Image = item.getImageResource();
            if (base64Image != null && !base64Image.isEmpty()) {
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                itemImage.setImageBitmap(bitmap);
            }

            itemView.setOnClickListener(v -> {
                RecycleDatailInfoFragment detailFragment = RecycleDatailInfoFragment.newInstance(
                        item.getName(),
                        item.getImageResource(),
                        item.getDetail()
                );
                detailFragment.show(getSupportFragmentManager(), detailFragment.getTag());
            });
        });
        recyclerView.setAdapter(adapter);

        int selectedTab = getIntent().getIntExtra("selectedTab", 0);
        if (selectedTab >= 0 && selectedTab <= 5) {
            loadAllItems(selectedTab);
        }

        btnHowToRecycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LearnMore.this, HowToRecycleActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnOpenLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LearnMore.this, RecivingPoint.class);
                startActivity(intent);
                finish();
            }
        });

        tvRecycle.setOnClickListener(view -> applyFilter(1)); // Tái chế
        tvNonRecycle.setOnClickListener(view -> applyFilter(0));
        btnBackLearnMore.setOnClickListener(v -> finish());

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadAllItems(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.selectTab(tabLayout.getTabAt(selectedTab));

        applyFilter(1);

    }

    // Tải dữ liệu mẫu
    protected void onResume() {
        super.onResume();
        loadAllItems(((TabLayout) findViewById(R.id.tabLayout)).getSelectedTabPosition());
    }
    // Tải dữ liệu mẫu
    private void loadAllItems(int selectedTab) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RecycleItem item = snapshot.getValue(RecycleItem.class);
                    allItems.add(item);
                }

                // Apply the filter after data is loaded
                getItemByCategory(selectedTab);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Failed to fetch data", databaseError.toException());
            }
        });
    }

    private void applyFilter(int isRecycle) {
        isRecycleItem = isRecycle;

        int selectedColor = getResources().getColor(R.color.white);

        if (isRecycle == 1) {
            tvRecycle.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT)); // Trong suốt
            tvNonRecycle.setBackgroundTintList(ColorStateList.valueOf(selectedColor)); // Màu trắng
            tvRecycle.setTextColor(getResources().getColor(R.color.white));
            tvNonRecycle.setTextColor(getResources().getColor(R.color.black));
        } else {
            tvNonRecycle.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT)); // Trong suốt
            tvRecycle.setBackgroundTintList(ColorStateList.valueOf(selectedColor)); // Màu trắng
            tvNonRecycle.setTextColor(getResources().getColor(R.color.white));
            tvRecycle.setTextColor(getResources().getColor(R.color.black));
        }

        getItemByCategory(((TabLayout) findViewById(R.id.tabLayout)).getSelectedTabPosition());
    }

    private void getItemByCategory(int categoryId) {
        List<RecycleItem> filteredItems = new ArrayList<>();

        for (RecycleItem item : allItems) {
            if (item.getCategory() == categoryId && item.getIsRecycle() == isRecycleItem) {
                filteredItems.add(item);
            }
        }

        adapter.updateData(filteredItems);
    }
}
