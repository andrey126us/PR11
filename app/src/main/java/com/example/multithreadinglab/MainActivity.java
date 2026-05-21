package com.example.multithreadinglab;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MultithreadingLab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCalculate = findViewById(R.id.btnCalculate);
        Button btnCalculateThread = findViewById(R.id.btnCalculateThread);
        Button btnLoadImage = findViewById(R.id.btnLoadImage);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageView imageView = findViewById(R.id.imageView);

        // Задание 2: без потоков (зависание)
        btnCalculate.setOnClickListener(v -> {
            longCalculation();
            Toast.makeText(MainActivity.this, "Вычисления завершены", Toast.LENGTH_SHORT).show();
        });
        Button btnIndividual = findViewById(R.id.btnIndividual);
        btnIndividual.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, IndividualActivity.class)));

        // Задание 3: с потоком
        btnCalculateThread.setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    longCalculation();
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Вычисления завершены", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        // Задание 4: загрузка изображения с прогрессом
        btnLoadImage.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Имитация прогресса (не связана с реальной загрузкой)
                        for (int i = 0; i <= 100; i += 10) {
                            Thread.sleep(200);
                            final int progress = i;
                            runOnUiThread(() -> progressBar.setProgress(progress));
                        }

                        // Загрузка реального изображения
                        // Замени URL на любой рабочий, если этот недоступен
                        Bitmap bitmap = loadImage("https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }).start();
        });
    }

    private void longCalculation() {
        long result = 0;
        for (long i = 0; i < 2_000_000_000L; i++) {
            result += i;
        }
        Log.d(TAG, "Результат: " + result);
    }

    private Bitmap loadImage(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();
        return bitmap;
    }
}