package com.example.regreen.myapplication.Admin.GenericMethod;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.regreen.myapplication.ModelData.News;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirebaseRepository<T> {
    private static final String TAG = "FirebaseRepository";
    private DatabaseReference reference;
    private Class<T> modelClass;

    public FirebaseRepository(String path, Class<T> modelClass) {
        this.reference = FirebaseDatabase.getInstance().getReference(path);
        this.modelClass = modelClass;
    }

    public DatabaseReference getReference() {
        return reference;
    }

    public void save(String key, T data, OnOperationListener listener) {
        reference.child(key).setValue(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Data saved successfully at key: " + key);
                listener.onSuccess("Lưu dữ liệu thành công.");
            } else {
                Log.e(TAG, "Failed to save data at key: " + key);
                listener.onFailure("Không thể lưu dữ liệu.");
            }
        });
    }

    public void get(String key, OnFetchListener<T> listener) {
        reference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        T data = snapshot.getValue(modelClass);
                        listener.onSuccess(data);
                    } catch (Exception e) {
                        Log.e(TAG, "Data conversion error in get at key: " + key + " - " + e.getMessage());
                        listener.onFailure("Lỗi chuyển đổi dữ liệu.");
                    }
                } else {
                    listener.onFailure("Không tìm thấy dữ liệu.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onFailure("Lỗi khi lấy dữ liệu: " + error.getMessage());
            }
        });
    }

    public void update(String key, Map<String, Object> updates, OnOperationListener listener) {
        reference.child(key).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Data updated successfully at key: " + key);
                listener.onSuccess("Cập nhật dữ liệu thành công.");
            } else {
                Log.e(TAG, "Failed to update data at key: " + key);
                listener.onFailure("Không thể cập nhật dữ liệu.");
            }
        });
    }

    public void delete(String key, OnOperationListener listener) {
        reference.child(key).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Data deleted successfully at key: " + key);
                listener.onSuccess("Xóa dữ liệu thành công.");
            } else {
                Log.e(TAG, "Failed to delete data at key: " + key);
                listener.onFailure("Không thể xóa dữ liệu.");
            }
        });
    }

    public String getNewKey() {
        return reference.push().getKey();
    }

    public void getAll(OnFetchListListener<T> listener) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> dataList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        T data = dataSnapshot.getValue(modelClass);
                        if (data != null) {
                            dataList.add(data);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Data conversion error in getAll at key: " + dataSnapshot.getKey()
                                + " - Expected class: " + modelClass.getSimpleName() + ", Error: " + e.getMessage());
                    }
                }
                if (dataList.isEmpty()) {
                    listener.onFailure("Không có dữ liệu nào.");
                } else {
                    listener.onSuccess(dataList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Lỗi khi lấy danh sách dữ liệu: " + error.getMessage());
            }
        });
    }

    public void getAllRealTime(OnFetchListListener<T> listener) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> dataList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        T data = dataSnapshot.getValue(modelClass);
                        if (data != null) {
                            dataList.add(data);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Data conversion error in getAllRealTime at key: " + dataSnapshot.getKey()
                                + " - Expected class: " + modelClass.getSimpleName() + ", Error: " + e.getMessage());
                    }
                }
                if (dataList.isEmpty()) {
                    listener.onFailure("Không có dữ liệu nào.");
                } else {
                    listener.onSuccess(dataList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Lỗi khi lấy danh sách dữ liệu: " + error.getMessage());
            }
        });
    }

    public void getItemsWithFilter(String field, String value, OnFetchListListener<T> listener) {
        Query query = reference.orderByChild(field).equalTo(value);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> dataList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        T data = dataSnapshot.getValue(modelClass);
                        if (data != null) {
                            dataList.add(data);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Data conversion error in getItemsWithFilter at key: "
                                + dataSnapshot.getKey() + " - Error: " + e.getMessage());
                    }
                }
                if (dataList.isEmpty()) {
                    listener.onFailure("Không có dữ liệu nào phù hợp.");
                } else {
                    listener.onSuccess(dataList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Lỗi khi lọc dữ liệu: " + error.getMessage());
            }
        });
    }

    public interface OnOperationListener {
        void onSuccess(String message);
        void onFailure(String message);
    }

    public interface OnFetchListener<T> {
        void onSuccess(T data);
        void onFailure(String message);
    }

    public interface OnFetchListListener<T> {
        void onSuccess(List<T> data);
        void onFailure(String message);
    }


    public void getLatestItems(int limit, OnFetchListListener<T> listener) {
        Query query = reference.orderByKey().limitToLast(limit);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<T> dataList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    T data = dataSnapshot.getValue(modelClass);
                    if (data != null) {
                        dataList.add(0, data); // Add each item to the start of the list to maintain order
                    }
                }
                listener.onSuccess(dataList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure("Error fetching latest items: " + error.getMessage());
            }
        });
    }


}
