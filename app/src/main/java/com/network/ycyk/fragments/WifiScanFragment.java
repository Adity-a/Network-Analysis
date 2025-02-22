package com.network.ycyk.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.network.ycyk.R;
import com.network.ycyk.adapter.WifiListAdapter;

import java.util.ArrayList;
import java.util.List;

public class WifiScanFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private WifiManager wifiManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView connectedWifiInfo;
    private Button scanButton;
    private ListView wifiListView;

    private WifiListAdapter wifiListAdapter;
    private List<ScanResult> wifiScanResults;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_scan, container, false);

        // Initialize views
        connectedWifiInfo = view.findViewById(R.id.connected_wifi_info);
        scanButton = view.findViewById(R.id.scan_button);
        wifiListView = view.findViewById(R.id.wifi_list_view);
        swipeRefreshLayout = view.findViewById(R.id.refresh);

        swipeRefreshLayout.setOnChildScrollUpCallback((parent, child) -> wifiListView.canScrollVertically(-1));

        // Initialize WiFi manager
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Initialize adapter and list
        wifiScanResults = new ArrayList<>();
        wifiListAdapter = new WifiListAdapter(requireContext(), wifiScanResults);
        wifiListView.setAdapter(wifiListAdapter);

        // Check and request permissions
        checkAndRequestPermissions();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            checkAndRequestPermissions();
            initializeWifiScan();
            swipeRefreshLayout.setRefreshing(false); // Stop the refresh indicator
        });

        scanButton.setOnClickListener(v -> performWifiScan());

        return view;
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CHANGE_WIFI_STATE);
        }

        // Handle Storage Permission based on Android version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {  // Android 9 (Pie) and below
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  // Android 11+
            if (!Environment.isExternalStorageManager()) {
                requestManageExternalStoragePermission();
                return;
            }
        }

        // Request only the necessary permissions
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsNeeded.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            initializeWifiScan();
        }
    }

    private void requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
    }

    private void initializeWifiScan() {
        if (!wifiManager.isWifiEnabled()) {
            //Toast.makeText(getContext(), "Enabling WiFi...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
        // Display connected Wi-Fi info
        WifiInfo connectedWifi = wifiManager.getConnectionInfo();
        if (connectedWifi != null && connectedWifi.getSSID() != null) {
            connectedWifiInfo.setText("Connected to: " + connectedWifi.getSSID());
            //performWifiScan();
        } else {
            connectedWifiInfo.setText("Not connected to any Wi-Fi network.");
        }
    }

    private void performWifiScan() {
        wifiManager.startScan();
        wifiScanResults = wifiManager.getScanResults();
        //Log.d("WifiScan", "Found " + wifiScanResults.size() + " access points.");

        if (!wifiScanResults.isEmpty()) {
            wifiListAdapter.updateScanResults(wifiScanResults);
            Toast.makeText(getContext(), "WiFi Scan Complete", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "No access points found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allPermissionsGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            // Special case for Android 11+ (Storage Management)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    allPermissionsGranted = false;
                    Toast.makeText(getContext(), "Storage access is required for backup. Please enable 'Manage all files' permission in settings.", Toast.LENGTH_LONG).show();
                    requestManageExternalStoragePermission();
                }
            }

            if (allPermissionsGranted) {
                // All permissions granted, proceed with Wi-Fi scanning and file operations
                initializeWifiScan();
                Toast.makeText(getContext(), "All required permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                // Check which permissions were denied and notify the user
                boolean storageDenied = Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                        (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
                boolean locationDenied = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;

                if (storageDenied && locationDenied) {
                    Toast.makeText(getContext(), "Storage and Location permissions denied. Backup and Wi-Fi scanning won't work.", Toast.LENGTH_LONG).show();
                } else if (storageDenied) {
                    Toast.makeText(getContext(), "Storage permission denied. Backup feature won't work.", Toast.LENGTH_LONG).show();
                } else if (locationDenied) {
                    Toast.makeText(getContext(), "Location permission denied. Wi-Fi scanning won't work.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
