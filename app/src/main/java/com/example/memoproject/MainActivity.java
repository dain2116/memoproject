package com.example.memoproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.location.Geocoder;
import android.location.Location;
import android.location.Address;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static String TAG = "MainActivity";

    Context context;

    // 날짜 시계 표시
    Timer timer;
    Handler handler;
    TextView dateNowTextView;

    // 주소 표시
    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    LocationManager locationManager;
    LocationListener locationListener;
    TextView addressTextView;
    Button addressButton;

    // 메모 저장 불러오기
    Button loadButton;
    Button saveButton;
    Button deleteButton;
    EditText inputText;

    class MainTimerTask extends TimerTask {
        public void run() {
            Message message = handler.obtainMessage();
            handler.sendMessage(message);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // 날짜 시계 표시
        handler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                Date dateNow = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String formatDate = dateFormat.format(dateNow);
                dateNowTextView = (TextView) findViewById(R.id.dateNowTextView);
                dateNowTextView.setText(formatDate);
                return true;
            }
        });

        timer = new Timer();
        timer.schedule(new MainTimerTask(), 500, 1000);


        // 주소 표시
        addressTextView = (TextView) findViewById(R.id.addressTextView);
        addressButton = (Button) findViewById(R.id.addressButton);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) {
                addressTextView.setText("현재 위치정보 이용가능");
            }
            public void onProviderDisabled(String provider) {
                addressTextView.setText("현재 위치정보 이용불가");
            }

            @Override
            public void onLocationChanged(@NonNull Location location) {
            }
        };

        addressButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 0 );
                }
                else {
                    Location location = null;
                    if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }

                    if(location != null) {
                        locationManager.requestLocationUpdates(location.getProvider(), 1000, 1, locationListener);

                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();

                        Geocoder geocoder = new Geocoder(context, Locale.KOREA);
                        List<Address> addressList;
                        String addressString = "주소찾기 실패.";

                        try {
                            if(geocoder != null){
                                addressList = geocoder.getFromLocation(latitude, longitude, 1);
                                if(addressList != null && addressList.size() > 0) {
                                    addressString = addressList.get(0).getAddressLine(0).toString();
                                }
                            }
                            addressTextView.setText(addressString);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        // 메모 저장 불러오기
        loadButton = (Button) findViewById(R.id.load);
        saveButton = (Button) findViewById(R.id.save);
        deleteButton = (Button) findViewById(R.id.delete);
        inputText = (EditText) findViewById(R.id.inputText);

        loadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FileInputStream fis = null;
                try {
                    fis = openFileInput("memo.txt");
                    byte[] data = new byte[fis.available()];
                    while (fis.read(data) != -1) {
                    }
                    inputText.setText(new String(data));
                    Toast.makeText(getApplicationContext(), "load completed", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fis != null) fis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput("memo.txt", MODE_PRIVATE);
                    String out = inputText.getText().toString();
                    fos.write(out.getBytes());
                    Toast.makeText(getApplicationContext(), "save completed", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fos != null) fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean b = deleteFile("memo.txt");
                if (b) {
                    Toast.makeText(getApplicationContext(), "delete completed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "delete failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //timer.schedule(new MainTimerTask(), 500, 1000);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //timer.cancel();

        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        timer.cancel();

        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}