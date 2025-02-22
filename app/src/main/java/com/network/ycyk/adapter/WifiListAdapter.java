package com.network.ycyk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.network.ycyk.R;

import java.util.List;

public class WifiListAdapter extends android.widget.BaseAdapter {
    private final Context context;
    private List<ScanResult> wifiScanResults;

    public WifiListAdapter(Context context, List<ScanResult> wifiScanResults) {
        this.context = context;
        this.wifiScanResults = wifiScanResults;
    }

    @Override
    public int getCount() {
        return wifiScanResults.size();
    }

    @Override
    public Object getItem(int position) {
        return wifiScanResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.wifi_list_item, parent, false);
        }

        ScanResult result = wifiScanResults.get(position);

        TextView ssidView = convertView.findViewById(R.id.ssid);
        TextView macAddressView = convertView.findViewById(R.id.bssid);
        TextView signalView = convertView.findViewById(R.id.signal_strength);
        TextView distanceView = convertView.findViewById(R.id.distance);
        TextView frequencyBandView = convertView.findViewById(R.id.frequency_band);


        // Populate SSID and Signal Strength
        ssidView.setText(result.SSID);
        macAddressView.setText( result.BSSID);
        int rssi = result.level;

        // Determine signal strength level and change color
        String signalStrengthText = "Signal: " + rssi + " dBm";
        signalView.setText(signalStrengthText);
        int color = getSignalStrengthColor(rssi);
        signalView.setTextColor(color);

        // Calculate and display distance
        int txPower = -40; // Typical Tx Power for Wi-Fi (customize if you know the actual Tx Power)
        double distance = calculateDistance(rssi, txPower);
        distanceView.setText(String.format("Distance: %.2f m", distance));

        // Determine frequency band and display it
        String frequencyBand = getFrequencyBand(result.frequency);
        frequencyBandView.setText(frequencyBand);

        return convertView;
    }

    public void updateScanResults(List<ScanResult> newResults) {
        this.wifiScanResults = newResults;
        notifyDataSetChanged();
    }

    private String getFrequencyBand(int frequency) {
        // Define Wi-Fi frequency bands
        if (frequency >= 2400 && frequency <= 2500) {
            return "2.4 GHz";
        } else if (frequency >= 4900 && frequency <= 5900) {
            return "5 GHz";
        } else {
            return "UBand";
        }
    }

    private double calculateDistance(int rssi, int txPower) {
        // txPower: assumed signal strength at 1 meter, typically around -30 to -40 dBm
        // RSSI:(Received Signal Strength Indicator (in dBm))
        int signalPropagationConstant = 2; // 2 for indoor (line of sight), higher for obstructed environments

        return Math.pow(10, (txPower - rssi) / (10.0 * signalPropagationConstant));
    }

    private int getSignalStrengthColor(int rssi) {
        if (rssi >= -50) {
            // Strong signal
            return Color.GREEN;
        } else if (rssi >= -70) {
            // Medium signal
            return Color.YELLOW;
        } else {
            // Weak signal
            return Color.RED;
        }
    }
}
