package com.example.regreen.myapplication.Admin.Recycle;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.RecycleItem;
import com.example.regreen.myapplication.MyAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecycleManagerActivity extends AppCompatActivity {

    private TextView tvRecycle, tvNonRecycle;
    private RecyclerView recyclerView;
    private ImageButton btnBackRecycle;
    private MyAdapter<RecycleItem> adapter;
    private List<RecycleItem> allItems = new ArrayList<>();
    private int isRecycleItem = 1;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Recycles");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_manager);

        //getWindow().setStatusBarColor(getResources().getColor(R.color.green_bg, null));

        tvRecycle = findViewById(R.id.tvRecycle2);
        tvNonRecycle = findViewById(R.id.tvNonRecycle2);
        recyclerView = findViewById(R.id.list_recycle_items2);
        btnBackRecycle = findViewById(R.id.btnBackRecycle);

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
                RecycleDetailFragment detailFragment = RecycleDetailFragment.newInstance(
                        item.getIdRecycle(),
                        item.getCategory(),
                        item.getIsRecycle(),
                        item.getName(),
                        item.getDetail(),
                        item.getImageResource()
                );
                detailFragment.show(getSupportFragmentManager(), detailFragment.getTag());
            });
        });
        recyclerView.setAdapter(adapter);

        Button btnAddRecycle = findViewById(R.id.btnAddRecycle);
        btnAddRecycle.setOnClickListener(v -> {
            AddRecycleFragment addRecycleFragment = new AddRecycleFragment();
            addRecycleFragment.show(getSupportFragmentManager(), addRecycleFragment.getTag());
        });

        tvRecycle.setOnClickListener(view -> applyFilter(1));
        tvNonRecycle.setOnClickListener(view -> applyFilter(0));
        btnBackRecycle.setOnClickListener(v -> finish());

        TabLayout tabLayout = findViewById(R.id.tabLayout2);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                getItemByCategory(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        tabLayout.selectTab(tabLayout.getTabAt(0));

        applyFilter(1);

        loadAllItems();
    }

    public void loadAllItems() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allItems.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RecycleItem item = snapshot.getValue(RecycleItem.class);
                    allItems.add(item);
                }

                getItemByCategory(0);
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

        getItemByCategory(((TabLayout) findViewById(R.id.tabLayout2)).getSelectedTabPosition());
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