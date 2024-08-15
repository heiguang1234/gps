package com.example.gps;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SecondActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private TextView locationInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seconed);

        locationInfoTextView = findViewById(R.id.location_info);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 检查并请求位置权限
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // 请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 权限已授予，开始获取位置
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        // 确保再次检查权限，以防止用户在请求权限后改变其决定
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            System.out.println("Location permission not granted.");
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 获取位置信息并显示在 TextView 中
            String address = getAddressFromLocation(location.getLatitude(), location.getLongitude());
            locationInfoTextView.setText(address);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 可选处理
        }

        @Override
        public void onProviderEnabled(String provider) {
            // 可选处理
        }

        @Override
        public void onProviderDisabled(String provider) {
            // 可选处理
        }
    };

    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressString = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressString.append(address.getAddressLine(i)).append("\n");
                }

                return addressString.toString();
            } else {
                return "No address found for this location.";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Unable to get address for this location.";
        }
    }


}
