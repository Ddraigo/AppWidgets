package com.example.regreen.myapplication.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.regreen.R;
import com.example.regreen.myapplication.ChangePassWord;
import com.example.regreen.myapplication.EditProfile;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.Login;
import com.example.regreen.myapplication.ModelData.Session;
import com.example.regreen.myapplication.ModelData.User;

public class ProfileFragment extends Fragment {

    private TextView tvUserName;
    private ImageView imgUserAvatar;
    private FirebaseRepository<User> userRepository;
    private FirebaseImageHelper imageHelper;
    public ProfileFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = new FirebaseRepository<>("User", User.class);
        imageHelper = new FirebaseImageHelper(requireContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button btnEditPassWord = view.findViewById(R.id.btnDoiMatKhau);
        Button btnLogout = view.findViewById(R.id.btnDangXuat);
        tvUserName = view.findViewById(R.id.tvUserName1);
        imgUserAvatar = view.findViewById(R.id.imgViewAvatar);

        final String userEmail;
        if (getArguments() != null) {
            userEmail = getArguments().getString("userEmail");
        } else {
            Toast.makeText(getContext(), "Không thể xác định người dùng đăng nhập!", Toast.LENGTH_SHORT).show();
            return view;
        }

        btnEditPassWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePassWord changePassWordDialog = new ChangePassWord();

                // Pass the email through Bundle
                Bundle bundle = new Bundle();
                bundle.putString("userEmail", userEmail);
                changePassWordDialog.setArguments(bundle);

                // Show the bottom sheet dialog
                changePassWordDialog.show(getParentFragmentManager(), changePassWordDialog.getTag());
            }
        });


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent= new Intent(getActivity(), Login.class);
                intent.putExtra("Logout","true");
                startActivity(intent);
            }
        });

        ImageButton imgBtnEditProfile = view.findViewById(R.id.imgBtnEditProfile);
        imgBtnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfile.class);
            intent.putExtra("userEmail", userEmail); // Passing the email
            startActivity(intent);
        });

        loadUserProfile(userEmail);

        return view;
    }


    private void loadUserProfile(String userEmail) {
        String emailPath = userEmail.replace(".", ",");
        userRepository.get(emailPath, new FirebaseRepository.OnFetchListener<User>() {
            @Override
            public void onSuccess(User user) {
                tvUserName.setText(user.getName());

                if (user.getAvatar() != null) {
                    displayBase64Image(user.getAvatar(), imgUserAvatar);
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "Không thể tải hồ sơ: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Toast.makeText(getContext(), "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
        }
    }
}

