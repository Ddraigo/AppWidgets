package com.example.regreen.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;


import com.example.regreen.R;
import com.example.regreen.databinding.ActivityLoginBinding;
import com.example.regreen.databinding.ActivitySignInBinding;
import com.example.regreen.myapplication.ModelData.Session;
import com.example.regreen.myapplication.ModelData.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Login extends AppCompatActivity {
    ActivityLoginBinding biding;
    FirebaseDatabase db;
    DatabaseReference reference;
    String email,password;
    EditText Password,Email;
    Session session;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";

    CheckBox checkBox;
    ImageButton LoginGg;
    private GoogleSignInClient signInClient;
    private  final  int REQUEST_CODE=20;
    private  FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        biding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(biding.getRoot());

        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_login, null));

        firebaseAuth = FirebaseAuth.getInstance();
        LoginGg = findViewById(R.id.LoginGg);
        checkBox = findViewById(R.id.checkBox_Login);
        Email = findViewById(R.id.editText_LoginEmail);
        Password = findViewById(R.id.editText_LoginPassword);

        Password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        Password.setCompoundDrawablesWithIntrinsicBounds(
                null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_key_24, null), null); // "eye closed" icon

        Password.setOnTouchListener((v, event) -> {
            Drawable rightDrawable = Password.getCompoundDrawables()[2];

            if (rightDrawable == null) {
                return false;
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableRightWidth = rightDrawable.getBounds().width();
                if (event.getRawX() >= (Password.getRight() - drawableRightWidth)) {
                    if (Password.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                        Password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        Password.setTransformationMethod(null);
                        Password.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_key_off_24, null), null); // "eye open" icon
                    } else {
                        Password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        Password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        Password.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_key_24, null), null); // "eye closed" icon
                    }

                    Password.setSelection(Password.getText().length());

                    Password.performClick();

                    return true;
                }
            }
            return false;
        });

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadCredentials();
        Intent intent = getIntent();
        if(intent.getStringExtra("Logout")!=null&&intent.getStringExtra("Logout").equals("true")){
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Login.this);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.clear();
//            editor.apply();
            Email.setText("");
            Password.setText("");
            checkBox.setChecked(false);

        }

        // Initialize Google Sign-In
        CreateRequest(); // Ensure you call this to initialize signInClient

        biding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = biding.editTextLoginEmail.getText().toString();
                password = biding.editTextLoginPassword.getText().toString();
                if (!email.isEmpty() && !password.isEmpty()) {

                    readData(email, password);
                } else if (email.isEmpty()) {
                    Toast.makeText(Login.this, "Vui lòng điền Email", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(Login.this, "Vui lòng điền mật khẩu", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Login.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LoginGg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInGoogle();
            }
        });

        TextView tvRegister = findViewById(R.id.textViewSignIn);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the SignIn fragment
                SignIn signInFragment = new SignIn();
                signInFragment.show(getSupportFragmentManager(), "SignInFragment");
            }
        });

        TextView tvForgetPassWord = findViewById(R.id.textView_LoginForgetPassWord);
        tvForgetPassWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgotPassWord forgotPassWord = new ForgotPassWord();
                forgotPassWord.show(getSupportFragmentManager(), "ForgotPassWordFragment");
            }
        });

    }

    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void loadCredentials() {
        if (sharedPreferences.getBoolean("remember", false)) {
            Email.setText(sharedPreferences.getString("email", ""));
            Password.setText(sharedPreferences.getString("password", ""));
            checkBox.setChecked(true);
        }
    }

    public void clearCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }
    private void SignInGoogle() {
        if (signInClient != null) {
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Log.e("SignInGoogle", "signInClient is null. Make sure to call CreateRequest() before this method.");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthwithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "Sign-in failed with error code: " + e.getStatusCode(), e);
                if (e.getStatusCode() == 10) {
                    Toast.makeText(this, "Google Sign-In failed due to configuration issues. Please check OAuth Client ID and SHA keys.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Đăng nhập bằng Google thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private void firebaseAuthwithGoogle(String account) {
        AuthCredential credential= GoogleAuthProvider.getCredential((account),null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user= firebaseAuth.getCurrentUser();
                    Map<String, Object> map = new HashMap<>();
                    map.put("id",user.getUid());
                    map.put("email",user.getEmail());
                    map.put("profile",user.getPhotoUrl().toString());
                    db.getReference().child("User").child(user.getUid()).setValue(map);
                    Intent intent= new Intent(Login.this,MainActivity.class);
                    startActivity(intent);

                    Toast.makeText(Login.this,"dang nhap bang google thanh cong",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login.this,"dang nhap gg fail",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void CreateRequest() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("812987981158-3lr9jimacbhj7nlmrdffdmm6qe651cbe.apps.googleusercontent.com")
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(Login.this, signInOptions); // Ensure this line is correct
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
                        String Password = String.valueOf(dataSnapshot.child("passWord").getValue());
                        String Email = String.valueOf(dataSnapshot.child("Email").getValue()).replace(".", ",");
                        if (password.equals(Password) ) {
                            Toast.makeText(Login.this,"Đăng nhập thành công!",Toast.LENGTH_SHORT).show();
                            if(checkBox.isChecked()){
                                saveCredentials(email,password);
                            }
                            else {
                                clearCredentials();
                            }
                            String userEmail=email;
                            Intent intent= new Intent(Login.this,MainActivity.class);
                            intent.putExtra("userEmail",userEmail);


                            startActivity(intent);
                        } else {
                            biding.editTextLoginPassword.setText("");
                            biding.textViewLoginErrorEmail.setVisibility(View.GONE);
                            biding.textViewLoginErrorPassWord.setVisibility(View.VISIBLE);
                        }
                    } else {
                        biding.editTextLoginEmail.setText("");
                        biding.editTextLoginPassword.setText("");
                        biding.textViewLoginErrorEmail.setVisibility(View.VISIBLE);
                        biding.textViewLoginErrorPassWord.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

}