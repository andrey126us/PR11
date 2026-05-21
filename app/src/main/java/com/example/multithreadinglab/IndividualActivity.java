package com.example.multithreadinglab;  // замени на свой пакет

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IndividualActivity extends AppCompatActivity {

    private EditText editSize;
    private Button btnCompute;
    private ProgressBar progressCompute;
    private TextView textResult;

    private Button btnLoadImages;
    private ProgressBar progressImages;
    private LinearLayout imagesContainer;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // Список URL изображений (можно менять)
    private final String[] imageUrls = new String[]{
            "https://www.lapseoftheshutter.com/wp-content/uploads/2021/10/practice-image-for-photoshop-lofoten-II-before.jpg",
            "https://www.lapseoftheshutter.com/wp-content/uploads/2021/10/practice-image-for-photoshop-stockholm-subway-after.jpg",
            "https://www.lapseoftheshutter.com/wp-content/uploads/2021/12/practice-image-for-photoshop-madrid-after.jpg",
            "https://www.lapseoftheshutter.com/wp-content/uploads/2021/10/practice-image-for-photoshop-kyoto-after.jpg",
            "https://www.lapseoftheshutter.com/wp-content/uploads/2021/10/practice-image-for-photoshop-lofoten-after.jpg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual);

        editSize = findViewById(R.id.editSize);
        btnCompute = findViewById(R.id.btnCompute);
        progressCompute = findViewById(R.id.progressCompute);
        textResult = findViewById(R.id.textResult);

        btnLoadImages = findViewById(R.id.btnLoadImages);
        progressImages = findViewById(R.id.progressImages);
        imagesContainer = findViewById(R.id.imagesContainer);

        // ---------- Часть 1: Вычисления ----------
        btnCompute.setOnClickListener(v -> {
            // Читаем размер массива, по умолчанию 100
            int size;
            try {
                size = Integer.parseInt(editSize.getText().toString().trim());
            } catch (NumberFormatException e) {
                size = 100;
            }
            if (size <= 0) size = 100;

            // Запускаем вычисления в фоновом потоке
            startComputation(size);
        });

        // ---------- Часть 2: Загрузка изображений ----------
        btnLoadImages.setOnClickListener(v -> startImageLoading());
    }

    // ================== Часть 1 ==================
    private void startComputation(int size) {
        // Показываем прогресс
        progressCompute.setVisibility(View.VISIBLE);
        progressCompute.setProgress(0);
        textResult.setText("Идёт вычисление...");

        new Thread(() -> {
            // Генерация массива случайных вещественных чисел [-10, 10)
            double[] array = new double[size];
            Random random = new Random();
            for (int i = 0; i < size; i++) {
                array[i] = (random.nextDouble() * 20) - 10;  // от -10 до 10
                // Имитация длительной работы (чтобы был виден прогресс)
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                // Обновляем прогресс
                final int progress = (int) ((i + 1) * 100.0 / size);
                mainHandler.post(() -> progressCompute.setProgress(progress));
            }

            // Вычисление минимального по модулю элемента
            double minAbs = Math.abs(array[0]);
            for (int i = 1; i < size; i++) {
                double abs = Math.abs(array[i]);
                if (abs < minAbs) {
                    minAbs = abs;
                }
            }

            // Сумма модулей элементов после первого нуля
            double sumAfterZero = 0.0;
            boolean foundZero = false;
            for (int i = 0; i < size; i++) {
                if (foundZero) {
                    sumAfterZero += Math.abs(array[i]);
                } else if (array[i] == 0.0) {
                    foundZero = true;
                }
            }
            // Если нуля нет, сумма = 0 (по условию)
            final double finalMinAbs = minAbs;
            final double finalSumAfterZero = sumAfterZero;
            final boolean zeroExists = foundZero;

            // Вывод результата в UI-потоке
            mainHandler.post(() -> {
                progressCompute.setVisibility(View.GONE);
                String resultText = "Размер массива: " + size + "\n"
                        + "Минимальный по модулю элемент: " + finalMinAbs + "\n"
                        + (zeroExists ? "Сумма модулей после первого нуля: " + finalSumAfterZero
                        : "Нулевой элемент отсутствует, сумма = 0");
                textResult.setText(resultText);
            });
        }).start();
    }

    // ================== Часть 2 ==================
    private void startImageLoading() {
        progressImages.setVisibility(View.VISIBLE);
        progressImages.setProgress(0);
        imagesContainer.removeAllViews(); // Очистка предыдущих загруженных изображений

        new Thread(() -> {
            int total = imageUrls.length;
            for (int i = 0; i < total; i++) {
                try {
                    // Загрузка изображения
                    Bitmap bitmap = loadImage(imageUrls[i]);
                    // Добавление ImageView в UI
                    mainHandler.post(() -> {
                        ImageView iv = new ImageView(IndividualActivity.this);
                        iv.setImageBitmap(bitmap);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                200, 200);
                        params.setMargins(0, 0, 0, 16);
                        iv.setLayoutParams(params);
                        imagesContainer.addView(iv);
                    });
                } catch (IOException e) {
                    mainHandler.post(() ->
                            Toast.makeText(IndividualActivity.this,
                                    "Ошибка загрузки: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
                }
                // Обновление общего прогресса
                final int progress = (int) ((i + 1) * 100.0 / total);
                mainHandler.post(() -> progressImages.setProgress(progress));
                // Небольшая пауза между загрузками для наглядности
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            }
            mainHandler.post(() -> {
                progressImages.setVisibility(View.GONE);
                Toast.makeText(IndividualActivity.this, "Загрузка завершена", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    // Метод загрузки изображения из сети (как в учебной части)
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