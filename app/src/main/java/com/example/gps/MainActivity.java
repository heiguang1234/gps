package com.example.gps;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private TextView resultTextView;
    private LocationManager locationManager;
    private boolean isMockingLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);
        Button submitButton = findViewById(R.id.submitButton);
        resultTextView = findViewById(R.id.resultTextView);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 检查并请求权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 设置按钮点击事件
        submitButton.setOnClickListener(v -> {
            if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 1) {
                resultTextView.setText("Please enable mock location in developer options.");
                return;
            }

            String latitudeText = latitudeEditText.getText().toString();
            String longitudeText = longitudeEditText.getText().toString();

            if (!latitudeText.isEmpty() && !longitudeText.isEmpty()) {
                double latitude = Double.parseDouble(latitudeText);
                double longitude = Double.parseDouble(longitudeText);

                isMockingLocation = true;
                startMockingLocation(latitude, longitude);
            } else {
                resultTextView.setText("Please enter both latitude and longitude.");
            }
        });
    }

    private void startMockingLocation(double latitude, double longitude) {
        new Thread(() -> {
            while (isMockingLocation) {
                setMockLocation(latitude, longitude);

                try {
                    // 每隔 2 秒更新一次位置
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setMockLocation(double latitude, double longitude) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                Location mockLocation = new Location(LocationManager.GPS_PROVIDER);
                mockLocation.setLatitude(latitude);
                mockLocation.setLongitude(longitude);
                mockLocation.setAltitude(0);
                mockLocation.setTime(System.currentTimeMillis());
                mockLocation.setAccuracy(1);
                mockLocation.setElapsedRealtimeNanos(System.nanoTime());

                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);

                runOnUiThread(() -> resultTextView.setText("Mock location set:\nLatitude: " + latitude + "\nLongitude: " + longitude));
            } catch (SecurityException e) {
                runOnUiThread(() -> resultTextView.setText("Failed to set mock location: " + e.getMessage()));
            }
        } else {
            runOnUiThread(() -> resultTextView.setText("Location permission not granted."));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                resultTextView.setText("Location permission denied.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止模拟位置
        isMockingLocation = false;
    }
}
