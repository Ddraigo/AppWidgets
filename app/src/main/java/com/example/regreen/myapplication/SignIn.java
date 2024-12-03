package com.example.regreen.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.example.regreen.databinding.ActivitySignInBinding;
import com.example.regreen.myapplication.ModelData.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignIn extends BottomSheetDialogFragment {

    private ActivitySignInBinding binding;
    private String email, password, repeatPassword;
    private DatabaseReference reference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ActivitySignInBinding.inflate(inflater, container, false);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        reference = db.getReference("User");

        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.editTextEmail.getText().toString();
                password = binding.editTextPassWord.getText().toString();
                repeatPassword = binding.editTextRepeatPassWord.getText().toString();

                // Validate inputs
                if (!email.isEmpty() && !password.isEmpty() && repeatPassword.equals(password) && emailValid(email) && passwordValid(password)) {
                    User user = new User(email, password);
                    reference.child(email.replace(".", ",")).setValue(user) // replace "." with "," in the key
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        binding.editTextEmail.setText("");
                                        binding.editTextPassWord.setText("");
                                        binding.editTextRepeatPassWord.setText("");

                                        binding.editTextErrorPassWord.setVisibility(View.GONE);
                                        binding.eidtTextErrorEmail.setVisibility(View.GONE);
                                        binding.editTextErrorRepeatPassWord.setVisibility(View.GONE);

                                        Toast.makeText(getContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(getContext(), Login.class);
                                        startActivity(intent);
                                        dismiss(); // Close the BottomSheet
                                    } else {
                                        Toast.makeText(getContext(), "Failed to create account", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else if (!emailValid(email)) {
                    binding.eidtTextErrorEmail.setVisibility(View.VISIBLE);
                    binding.editTextEmail.setText("");
                    binding.editTextErrorPassWord.setVisibility(View.GONE);
                    binding.editTextErrorRepeatPassWord.setVisibility(View.GONE);
                } else if (!passwordValid(password)) {
                    binding.editTextErrorPassWord.setVisibility(View.VISIBLE);
                    binding.editTextPassWord.setText("");
                    binding.editTextRepeatPassWord.setText("");
                    binding.eidtTextErrorEmail.setVisibility(View.GONE);
                    binding.editTextErrorRepeatPassWord.setVisibility(View.GONE);
                } else if (!password.equals(repeatPassword)) {
                    binding.editTextErrorRepeatPassWord.setVisibility(View.VISIBLE);
                    binding.editTextRepeatPassWord.setText("");
                    binding.editTextErrorPassWord.setVisibility(View.GONE);
                    binding.eidtTextErrorEmail.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return binding.getRoot();
    }

    // Email validation function
    private boolean emailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Password validation function
    private boolean passwordValid(String password) {
        if (password.length() < 8) {
            return false;
        }

        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
