package com.example.memoproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    Button loadButton;
    Button saveButton;
    Button deleteButton;
    EditText inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadButton = (Button) findViewById(R.id.load);
        saveButton = (Button) findViewById(R.id.save);
        deleteButton = (Button) findViewById(R.id.delete);
        inputText = (EditText) findViewById(R.id.inputText);

        loadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FileInputStream fis = null;
                try{
                    fis = openFileInput("memo.txt");
                    byte[] data = new byte[fis.available()];
                    while( fis.read(data) != -1){
                    }
                    inputText.setText(new String(data));
                    Toast.makeText(getApplicationContext(), "load completed", Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    try{ if(fis != null) fis.close(); }catch(Exception e){e.printStackTrace();}
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FileOutputStream fos = null;
                try{
                    fos = openFileOutput("memo.txt", MODE_PRIVATE);
                    String out = inputText.getText().toString();
                    fos.write(out.getBytes());
                    Toast.makeText(getApplicationContext(), "save completed", Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    try{ if(fos != null) fos.close(); }catch(Exception e){e.printStackTrace();}
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean b = deleteFile("memo.txt");
                if(b){
                    Toast.makeText(getApplicationContext(), "delete completed", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "delete failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}