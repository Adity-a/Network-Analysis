package com.network.ycyk.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.network.ycyk.R;
import com.network.ycyk.adapter.DeviceAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchDeviceNetwork extends Fragment {

    private static final String TAG = "SearchDeviceNetwork";
    private Button scanButton;
    private ListView deviceListView;
    private DeviceAdapter deviceAdapter;
    private List<String> deviceList = new ArrayList<>();
    public boolean scanning = false; // Flag to track if scanning is in progress

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_device_network, container, false);

        scanButton = view.findViewById(R.id.scanButton);
        deviceListView = view.findViewById(R.id.devices_list_view);

        deviceAdapter = new DeviceAdapter(requireContext(), deviceList);
        deviceListView.setAdapter(deviceAdapter);

        scanButton.setOnClickListener(v -> new DeviceDiscoveryTask().execute());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Handle back press with condition based on scanning state
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (scanning) {
                    // Do nothing if scanning is in progress
                    Toast.makeText(getActivity(), "Scan in progress. Cannot go back!", Toast.LENGTH_SHORT).show();
                } else {
                    // Allow back press if no scanning is in progress
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private class DeviceDiscoveryTask extends AsyncTask<Void, String, List<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            scanning = true;
            Toast.makeText(requireContext(), "Scanning...", Toast.LENGTH_SHORT).show();
            deviceList.clear();
            deviceAdapter.notifyDataSetChanged();
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> devices = new ArrayList<>();
            try {
                String localIp = getLocalIpAddress();
                if (localIp != null) {
                    devices.addAll(scanNetworkForDevices(localIp));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during device discovery", e);
            }
            return devices;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            deviceList.add(values[0]);
            deviceAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(List<String> devices) {
            super.onPostExecute(devices);
            if (devices.isEmpty()) {
                Toast.makeText(requireContext(), "No devices found!", Toast.LENGTH_SHORT).show();
            } else {
                scanning = false;
                Toast.makeText(requireContext(), "Scan complete!", Toast.LENGTH_SHORT).show();
            }
        }

        private String getLocalIpAddress() {
            try {
                for (java.util.Enumeration<java.net.NetworkInterface> en = java.net.NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    java.net.NetworkInterface intf = en.nextElement();
                    for (java.util.Enumeration<java.net.InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        java.net.InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error getting local IP", ex);
            }
            return null;
        }

        private List<String> scanNetworkForDevices(String localIp) {
            List<String> devices = new ArrayList<>();
            String subnet = localIp.substring(0, localIp.lastIndexOf(".") + 1);
            ExecutorService executor = Executors.newFixedThreadPool(50);

            for (int i = 1; i <= 254; i++) {
                final String ip = subnet + i;
                executor.execute(() -> {
                    try {
                        if (pingIpAddress(ip)) {
                            StringBuilder result = new StringBuilder(ip);
                            List<Integer> openPorts = scanOpenPorts(ip, new int[]{20, 21, 22, 23, 25, 53, 80, 110, 123, 143, 161, 443, 587,
                                    993, 995, 1433, 1521, 1723, 3306, 3389, 5432, 8080, 8443,
                                    5000, 5900, 6379, 25565, 27017, 9090}); // Common ports
                            if (!openPorts.isEmpty()) {
                                result.append(" | Open Ports: ").append(openPorts);
                            }
                            devices.add(result.toString());
                            publishProgress(result.toString());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error scanning IP: " + ip, e);
                    }
                });
            }

            executor.shutdown();
            try {
                executor.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for tasks to finish", e);
            }
            return devices;
        }

        private boolean pingIpAddress(String ip) {
            try {
                Process process = Runtime.getRuntime().exec("ping -c 1 " + ip);
                int returnCode = process.waitFor();
                return returnCode == 0; // Return true if ping was successful
            } catch (Exception e) {
                Log.e(TAG, "Ping failed for IP: " + ip, e);
                return false;
            }
        }

        private List<Integer> scanOpenPorts(String ip, int[] ports) {
            List<Integer> openPorts = new ArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(10);

            for (int port : ports) {
                executor.execute(() -> {
                    try {
                        if (isPortOpen(ip, port, 300)) { // Timeout set to 200ms
                            openPorts.add(port);
                        }
                    } catch (Exception e) {
                        // Ignore errors for closed ports
                    }
                });
            }

            executor.shutdown();
            try {
                executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for port scanning tasks", e);
            }
            return openPorts;
        }

        private boolean isPortOpen(String ip, int port, int timeout) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, port), timeout);
                return true; // Connection successful, port is open
            } catch (IOException e) {
                return false; // Port is closed
            }
        }
    }
}
