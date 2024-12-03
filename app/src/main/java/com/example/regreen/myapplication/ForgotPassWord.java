package com.example.regreen.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.regreen.R;
import com.example.regreen.databinding.ActivityForgotPassWordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPassWord extends BottomSheetDialogFragment {

    ActivityForgotPassWordBinding binding;
    FirebaseDatabase db;
    DatabaseReference reference;
    String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityForgotPassWordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.editTextForgetPassWordEmail.getText().toString();

                if (!email.isEmpty()) {
                    readData(email);
                } else {
                    Toast.makeText(getContext(), "Vui lòng điền Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void readData(String email) {
        String emailPath = email.replace(".", ",");

        reference = FirebaseDatabase.getInstance().getReference("User");
        reference.child(emailPath).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        String Email = String.valueOf(dataSnapshot.child("email").getValue());
                        String Password = String.valueOf(dataSnapshot.child("passWord").getValue());

                        sendPasswordToEmail(Email, Password);
                    } else {
                        Toast.makeText(getContext(), "Email không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void sendPasswordToEmail(final String recipientEmail, final String userPassword) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Your email sending logic here
                    sendEmail(recipientEmail, userPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        executorService.shutdown();
    }

    private void sendEmail(String recipientEmail, String userPassword) throws Exception {
        String host = "smtp.gmail.com";
        String port = "587";
        final String senderEmail = "Huyduong11082004@gmail.com";
        final String senderPassword = "lipm cypm evln wrkg";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Password Reset");
            message.setText("Your password is: " + userPassword);

            // Send the email
            Transport.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new Exception("Error sending email: " + e.getMessage());
        }
    }
}
