package com.example.regreen.myapplication.Admin.AccessPoint;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.AccessPoint;
import com.example.regreen.myapplication.MyAdapter;

import java.util.ArrayList;
import java.util.List;

public class AccessPointManagerActivity extends AppCompatActivity {

    private RecyclerView rvReceivingPointList, rvCollectionPointList;
//    private MyAdapter<AccessPoint> receivingPointAdapter, collectionPointAdapter;
    private ImageButton btnBackManageAccessPoint;
    private Button btnAddReceivingPoint, btnAddCollectionPoint;

    private FirebaseRepository<AccessPoint> repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_point_manager);

        getWindow().setStatusBarColor(getResources().getColor(R.color.green_bg, null));

        rvReceivingPointList = findViewById(R.id.rvReceivingPointList);
        rvCollectionPointList = findViewById(R.id.rvCollectionPointList);
        btnBackManageAccessPoint = findViewById(R.id.btnBackManageAccessPoint);
        btnAddReceivingPoint = findViewById(R.id.btnAddReceivingPoint);
        btnAddCollectionPoint = findViewById(R.id.btnAddCollectionPoint);

        repository = new FirebaseRepository<>("AccessPoint", AccessPoint.class);

        rvReceivingPointList.setLayoutManager(new LinearLayoutManager(this));
        rvCollectionPointList.setLayoutManager(new LinearLayoutManager(this));

        btnBackManageAccessPoint.setOnClickListener(v -> finish());
        btnAddReceivingPoint.setOnClickListener(v -> openAddPointDialog("ReceivingPoints"));
        btnAddCollectionPoint.setOnClickListener(v -> openAddPointDialog("CollectionPoints"));

        loadAccessPoints();
    }

    private void openAddPointDialog(String type) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_access_point, null);
        builder.setView(dialogView);

        EditText edtAccessPoint = dialogView.findViewById(R.id.edtAccessPoint);
        Button btnAdd = dialogView.findViewById(R.id.btnAddAccessPoint);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelAccessPoint);

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

        btnAdd.setOnClickListener(v -> {
            String pointName = edtAccessPoint.getText().toString().trim();
            if (pointName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên điểm", Toast.LENGTH_SHORT).show();
            } else {
                addAccessPointToFirebase(pointName, type); // Truyền loại vào đây
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void addAccessPointToFirebase(String pointName, String category) {
        String id = "accessPoint_" + System.currentTimeMillis();
        AccessPoint accessPoint = new AccessPoint(id, pointName, category);

        repository.save(id, accessPoint, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(AccessPointManagerActivity.this, "Thêm điểm thành công!", Toast.LENGTH_SHORT).show();
                loadAccessPoints();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AccessPointManagerActivity.this, "Không thể thêm điểm: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadAccessPoints() {
        repository.getAllRealTime(new FirebaseRepository.OnFetchListListener<AccessPoint>() {
            @Override
            public void onSuccess(List<AccessPoint> accessPoints) {
                List<AccessPoint> receivingPoints = new ArrayList<>();
                List<AccessPoint> collectionPoints = new ArrayList<>();

                for (AccessPoint point : accessPoints) {
                    if ("ReceivingPoints".equals(point.getCategory())) {
                        receivingPoints.add(point);
                    } else if ("CollectionPoints".equals(point.getCategory())) {
                        collectionPoints.add(point);
                    }
                }

                // Truyền category khi gọi setupRecyclerView
                setupRecyclerView(rvReceivingPointList, receivingPoints, "ReceivingPoints");
                setupRecyclerView(rvCollectionPointList, collectionPoints, "CollectionPoints");
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AccessPointManagerActivity.this, "Lỗi khi tải dữ liệu: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView, List<AccessPoint> data, String category) {
        if (data == null || data.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            if (recyclerView.getAdapter() == null) {
                MyAdapter<AccessPoint> adapter = new MyAdapter<>(data, R.layout.layout_item_access_point, (itemView, item) -> {
                    TextView tvAccessPoint = itemView.findViewById(R.id.tvAccessPoint);
                    tvAccessPoint.setText(item.getReceivingPoint());

                    // Mở Fragment chi tiết khi nhấn vào mục
                    itemView.setOnClickListener(v -> {
                        AccsessPointDetailFragment fragment = AccsessPointDetailFragment.newInstance(
                                item.getId(),
                                item.getReceivingPoint(), // Tên/địa chỉ của AccessPoint
                                category            // Loại (ReceivingPoints hoặc CollectionPoints)
                        );
                        fragment.show(getSupportFragmentManager(), "AccessPointDetail");
                    });
                });
                recyclerView.setAdapter(adapter);
            } else {
                ((MyAdapter<AccessPoint>) recyclerView.getAdapter()).updateData(data);
            }
        }
    }
}
