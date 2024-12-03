package com.example.regreen.myapplication.Admin.AccessPoint;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.AccessPoint;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;


public class AccsessPointDetailFragment extends BottomSheetDialogFragment {

    private String id, name, category;

    public AccsessPointDetailFragment() {}

    public static AccsessPointDetailFragment newInstance(String id, String name, String category) {
        AccsessPointDetailFragment fragment = new AccsessPointDetailFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("name", name);
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString("id");
            name = getArguments().getString("name");
            category = getArguments().getString("category");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accsess_point_detail, container, false);

        TextView edtAcPoint = view.findViewById(R.id.edtAcPoint);
        Button btnUpdateAcPoint = view.findViewById(R.id.btnUpdateAcPoint);
        Button btnDeleteAcPoint = view.findViewById(R.id.btnDeleteAcPoint);

        if (name != null) {
            edtAcPoint.setText(name);
        }

        btnUpdateAcPoint.setOnClickListener(v -> {
            String updatedName = edtAcPoint.getText().toString().trim();
            if (!updatedName.isEmpty()) {
                updateAccessPoint(id, updatedName);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên/địa chỉ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteAcPoint.setOnClickListener(v -> confirmDeleteAccessPoint(id));

        return view;
    }

    private void updateAccessPoint(String id, String updatedName) {
        FirebaseRepository<AccessPoint> repository = new FirebaseRepository<>("AccessPoint", AccessPoint.class);

        Map<String, Object> updates = new HashMap<>();
        updates.put("receivingPoint", updatedName);
        updates.put("category", category);

        repository.update(id, updates, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof AccessPointManagerActivity) {
                    ((AccessPointManagerActivity) getActivity()).loadAccessPoints(); // Tải lại danh sách
                }
                dismiss();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "Cập nhật thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteAccessPoint(String id) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa điểm thu/nhận")
                .setMessage("Bạn có chắc chắn muốn xóa điểm này?")
                .setPositiveButton("Có", (dialog, which) -> deleteAccessPoint(id))
                .setNegativeButton("Không", null)
                .show();
    }

    private void deleteAccessPoint(String id) {
        FirebaseRepository<AccessPoint> repository = new FirebaseRepository<>("AccessPoint", AccessPoint.class);

        repository.delete(id, new FirebaseRepository.OnOperationListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof AccessPointManagerActivity) {
                    ((AccessPointManagerActivity) getActivity()).loadAccessPoints(); // Tải lại danh sách
                }
                dismiss();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "Xóa thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}