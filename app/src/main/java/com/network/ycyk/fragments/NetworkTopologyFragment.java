package com.network.ycyk.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.network.ycyk.R;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkTopologyFragment extends Fragment {
    private TextView scanStatus;
    private TextView scanResult;
    private WebView webView; // WebView for displaying network topology
    private Button scanButton; // Button to trigger the network scan
    public boolean isScanning = false; // Flag to track if scanning is in progress

    private final CopyOnWriteArrayList<String> discoveredDevices = new CopyOnWriteArrayList<>(); // To store discovered devices

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_network_topology, container, false);

        scanStatus = view.findViewById(R.id.scan_status); // Status TextView
        scanResult = view.findViewById(R.id.scan_result); // Results TextView
        webView = view.findViewById(R.id.webView); // WebView for displaying network topology
        scanButton = view.findViewById(R.id.scan_button); // Scan button


        scanButton.setOnClickListener(v -> {
            webView.setWebViewClient(new WebViewClient()); // Open links in WebView
            webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript
            new NetworkScannerTask().execute();
        });

        return view; // Return the inflated view
    }
    @Override
    public void onResume() {
        super.onResume();

        // Handle back press with condition based on scanning state
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isScanning) {
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

    // AsyncTask to scan the local network for reachable devices
    private class NetworkScannerTask extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            isScanning = true;
            scanStatus.setText("Status: Scanning...");
            scanButton.setEnabled(false);// Update UI before the scan starts
            discoveredDevices.clear(); // Clear previously discovered devices
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String localIP = getLocalIpAddress(); // Get the local IP address
                if (localIP != null) {
                    String subnet = localIP.substring(0, localIP.lastIndexOf('.') + 1); // Get the network subnet

                    // Scan devices from 1 to 254
                    for (int i = 1; i < 255; i++) {
                        String testIP = subnet + i;
                        if (InetAddress.getByName(testIP).isReachable(100)) { // If IP is reachable, add it to the list
                            publishProgress(testIP); // Send the discovered IP for UI update
                        }
                    }
                }
            } catch (Exception e) {
                publishProgress("Error: " + e.getMessage()); // In case of an error, display it
            }
            return null; // Return nothing
        }

        @Override
        protected void onProgressUpdate(String... values) {
            String discoveredIP = values[0]; // Get the discovered IP address
            discoveredDevices.add(discoveredIP); // Add the device to the list
            updateUI(); // Update the UI after each discovery
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            isScanning = false; // Set flag to false when scan is finished
            scanStatus.setText("Status: Scan completed.");
            scanButton.setEnabled(true);// Update the status once the scan is done
            //updateUI(); // Update UI to reflect final result
        }
    }

    // Method to update the UI with the list of discovered devices
    private void updateUI() {
        // Update the device list TextView
        StringBuilder devicesText = new StringBuilder("Discovered Devices:\n");
        for (String device : discoveredDevices) {
            devicesText.append("- ").append(device).append("\n");
        }
        scanResult.setText(devicesText.toString()); // Display the devices in the TextView

        // Build the HTML content for WebView
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<script src='https://cdnjs.cloudflare.com/ajax/libs/vis/4.21.0/vis.min.js'></script>")
                .append("<style>")
                .append("#network { width: 100%; height: 600px; border: 1px solid lightgray; }") // Ensure the network div has a size
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div id='network'></div>") // Placeholder div for network
                .append("<script type='text/javascript'>")
                .append("var nodes = [")
                .append("{id: 0, label: 'Router'}"); // Add Router node

        // Add each discovered device to the nodes list
        int id = 1; // Start device IDs from 1
        for (String device : discoveredDevices) {
            htmlContent.append(", {id: ").append(id).append(", label: '").append(device).append("'}");
            id++;
        }

        htmlContent.append("];")
                .append("var edges = ["); // Edges to connect Router and devices

        // Connect the Router to each discovered device
        for (int i = 1; i < discoveredDevices.size() + 1; i++) {
            htmlContent.append("{from: 0, to: ").append(i).append("},");
        }

        // Close the edge definitions
        htmlContent.append("];")
                .append("var container = document.getElementById('network');")
                .append("var data = {nodes: nodes, edges: edges};")
                .append("var options = {edges: {smooth: false}};") // Non-smooth edges
                .append("var network = new vis.Network(container, data, options);")
                .append("</script>")
                .append("</body>")
                .append("</html>");

        // Load the HTML content into the WebView
        webView.loadDataWithBaseURL("", htmlContent.toString(), "text/html", "UTF-8", "");
    }

    // Method to get the local IP address of the Android device
    private String getLocalIpAddress() throws Exception {
        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface networkInterface = en.nextElement();
            for (Enumeration<InetAddress> ipEnum = networkInterface.getInetAddresses(); ipEnum.hasMoreElements(); ) {
                InetAddress inetAddress = ipEnum.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                    return inetAddress.getHostAddress(); // Return the local IP
                }
            }
        }
        return null; // Return null if no local IP is found
    }

}
