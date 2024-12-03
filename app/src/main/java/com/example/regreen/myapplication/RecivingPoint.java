package com.example.regreen.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.AccessPoint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class RecivingPoint extends AppCompatActivity {

    private GoogleMap mMap;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            loadReceivingPoints(); // Gọi hàm tải dữ liệu từ Firebase
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_maps);

        // Lấy SupportMapFragment từ layout
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void loadReceivingPoints() {
        FirebaseRepository<AccessPoint> repository = new FirebaseRepository<>("AccessPoint", AccessPoint.class);

        repository.getAll(new FirebaseRepository.OnFetchListListener<AccessPoint>() {
            @Override
            public void onSuccess(List<AccessPoint> accessPoints) {
                for (AccessPoint point : accessPoints) {
                    if ("ReceivingPoints".equals(point.getCategory())) {
                        addMarkerForAccessPoint(point);
                    }
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(RecivingPoint.this, "Không thể tải dữ liệu: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarkerForAccessPoint(AccessPoint point) {
        try {
            // Geocode để chuyển địa chỉ thành LatLng
            LatLng latLng = geocodeAddress(point.getReceivingPoint());
            if (latLng != null) {
                // Custom marker based on category or other criteria
                BitmapDescriptor icon = getCustomMarkerIcon(point);

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(point.getReceivingPoint())
                        .icon(icon));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            } else {
                Toast.makeText(this, "Không thể tìm thấy tọa độ cho: " + point.getReceivingPoint(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi hiển thị marker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng geocodeAddress(String address) {
        // Sử dụng Geocoder để chuyển đổi địa chỉ thành LatLng
        android.location.Geocoder geocoder = new android.location.Geocoder(this);
        try {
            List<android.location.Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                android.location.Address location = addressList.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private BitmapDescriptor getCustomMarkerIcon(AccessPoint point) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 150, 150, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }
}
