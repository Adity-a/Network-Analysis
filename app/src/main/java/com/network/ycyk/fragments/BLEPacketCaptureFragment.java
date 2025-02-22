package com.network.ycyk.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.network.ycyk.R;
import com.network.ycyk.service.PacketCaptureService;

public class BLEPacketCaptureFragment extends Fragment {
    private TextView logTextView;
    private Button startVpnButton, stopVpnButton;

    private final BroadcastReceiver intrusionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String alert = intent.getStringExtra("alert_message");
            logTextView.append("ALERT: " + alert + "\n\n");
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_packet_capture, container, false);

        logTextView = view.findViewById(R.id.logTextView);
        startVpnButton = view.findViewById(R.id.startVpnButton);
        stopVpnButton = view.findViewById(R.id.stopVpnButton);

        startVpnButton.setOnClickListener(v -> startVpn());
        stopVpnButton.setOnClickListener(v -> stopVpn());

        requireContext().registerReceiver(intrusionReceiver, new IntentFilter("com.network.ycyk.INTRUSION_ALERT"));

        return view;
    }

    private void startVpn() {
        Intent intent = VpnService.prepare(requireContext());
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, -1, null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == -1) {
            requireContext().startService(new Intent(requireContext(), PacketCaptureService.class));
            logTextView.append("VPN started...\n\n");
        }
    }

    private void stopVpn() {
        requireContext().stopService(new Intent(requireContext(), PacketCaptureService.class));
        logTextView.append("VPN stopped.\n\n");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(intrusionReceiver);
    }
}
