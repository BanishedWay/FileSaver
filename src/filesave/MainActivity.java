package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.utils.FileUtils;
import com.example.myapplication.utils.ReflectExample;


public class MainActivity extends AppCompatActivity {

    private EditText firstNumber;
    private EditText secondNumber;
    private TextView result;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "启动MainActivity");

        firstNumber = findViewById(R.id.editTextFirstNNUmber);
        secondNumber = findViewById(R.id.editTextSecondNumber);
        result = findViewById(R.id.resultTextView);

        Button button = findViewById(R.id.btnChange);

        button.setOnClickListener(v -> {
            Log.i(TAG, "按钮点击事件启动");

            new FileUtils(0).saveFile(this, "test", "test.txt", "test content");// 写入内部存储
            new FileUtils(1).saveFile(this, "test", "test.txt", "test content");
           // Log.d(TAG, "onCreate: " + content);
        });
    }

}
