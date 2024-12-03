package com.example.regreen.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.regreen.R;
import com.example.regreen.ml.ModelUnquant;
import com.example.regreen.myapplication.Fragment.ResutlFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WasteIdentification extends AppCompatActivity {
    ImageView imageView;
    Button picture;
    ImageButton btnBackScan;
    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(getResources().getColor(R.color.light_gray, null));
        // Firebase test value (optional)
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://goldenpet-58df5-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        mDatabase.child("test").setValue("Hello, Firebase!");

        setContentView(R.layout.activity_waste_identification);
        imageView = findViewById(R.id.imageView);
        picture = findViewById(R.id.btnTakePic);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        btnBackScan = findViewById(R.id.btnBackScan);
        btnBackScan.setOnClickListener(v -> finish());
    }

    public void classifyImage(Bitmap image) {
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Prepare input tensor
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // Normalize image pixels
            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255f));  // Red
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255f));   // Green
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255f));          // Blue
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();

            String[] classes = {"Glass", "Plastic", "Paper", "Metal", "Cardboard", "Battery", "Biological", "Other"};
            String[] recyclableClasses = {"Glass", "Plastic", "Paper", "Metal", "Cardboard", "Battery"};

            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String detectedClass;
            if (maxConfidence < 0.95) {
                detectedClass = "Other";
                maxPos = 7;  // Index for "Other"
            } else {
                detectedClass = classes[maxPos];
            }

            // Display result in a BottomSheetDialogFragment
            if (isRecyclable(detectedClass, recyclableClasses)==true) {
                showResultDialog( detectedClass, maxConfidence,"1");
            } else {
                showResultDialog( detectedClass, maxConfidence,"2");
            }

            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isRecyclable(String detectedClass, String[] recyclableClasses) {
        for (String recyclable : recyclableClasses) {
            if (recyclable.equals(detectedClass)) {
                return true;
            }
        }
        return false;
    }

    private void showResultDialog(String detectedClass, float confidence,String isRecycle) {
        // Tạo một instance của ResutlFragment với dữ liệu
        String confidenceText = String.format("%.2f%%", confidence * 100);
        ResutlFragment fragment = ResutlFragment.newInstance(detectedClass, confidenceText,isRecycle);

        // Hiển thị BottomSheetDialogFragment
        fragment.show(getSupportFragmentManager(), "result_fragment");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            imageView.setImageBitmap(image);
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyImage(image);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
