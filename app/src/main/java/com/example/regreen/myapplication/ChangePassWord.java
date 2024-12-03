package com.example.regreen.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.regreen.R;
import com.example.regreen.myapplication.ModelData.Session;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ChangePassWord extends BottomSheetDialogFragment {
    private EditText oldPass, newPass, repeatNewPass;
    private FirebaseDatabase db;
    private DatabaseReference reference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_change_password, container, false);

        oldPass = view.findViewById(R.id.edtInputPass);
        newPass = view.findViewById(R.id.edtNewPassWord);
        repeatNewPass = view.findViewById(R.id.edtRepeatPassWord);
        Button btnSaveChangePass = view.findViewById(R.id.btnSaveChangePass);

        // Get the email from ProfileFragment
        String userEmail = getArguments().getString("userEmail");

        btnSaveChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldPass.getText().toString().equals("") || newPass.getText().toString().equals("") || repeatNewPass.getText().toString().equals("")) {
                    oldPass.setError("Không được để trống");
                    newPass.setError("Không được để trống");
                    repeatNewPass.setError("Không được để trống");
                } else if (!newPass.getText().toString().equals(repeatNewPass.getText().toString())) {
                    repeatNewPass.setError("Mật khẩu không trùng khớp");
                } else {
                    readData(userEmail, oldPass.getText().toString());
                }
            }
        });

        return view;
    }

    private void readData(String email, String password) {
        String emailPath = email.replace(".", ",");

        reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child(emailPath).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        String storedPassword = String.valueOf(dataSnapshot.child("passWord").getValue());

                        if (password.equals(storedPassword)) {
                            Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                            reference.child(emailPath).child("passWord").setValue(newPass.getText().toString());
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
}

