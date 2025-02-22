package com.network.ycyk.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PacketCaptureService extends VpnService implements Runnable {
    private ParcelFileDescriptor vpnInterface;
    private Thread vpnThread;
    private boolean isRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        // This is required for VPNService
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (vpnThread == null) {
            vpnThread = new Thread(this, "PacketCaptureThread");
            vpnThread.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (vpnThread != null) {
            vpnThread.interrupt();
        }
        super.onDestroy();
    }

    @Override
    public void run() {
        try {
            Builder builder = new Builder();
            builder.addAddress("10.0.0.2", 32);
            builder.addRoute("0.0.0.0", 0);
            vpnInterface = builder.setSession("PacketCaptureVPN").establish();

            FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());

            ByteBuffer buffer = ByteBuffer.allocate(32767);
            isRunning = true;

            while (isRunning) {
                int length = in.read(buffer.array());
                if (length > 0) {
                    analyzePacket(buffer.array(), length);  // Analyze the captured packet
                }
                buffer.clear();
            }
        } catch (Exception e) {
            Log.e("VPNService", "Error: " + e.getMessage());
        }
    }

    private void analyzePacket(byte[] packet, int length) {
        if (length < 20) return; // Skip small packets
        Log.d("PacketAnalyzer", "Captured packet of length: " + length);

        int protocol = packet[9] & 0xFF; // Protocol (TCP=6, UDP=17)
        int srcPort = ((packet[20] & 0xFF) << 8) | (packet[21] & 0xFF);
        int dstPort = ((packet[22] & 0xFF) << 8) | (packet[23] & 0xFF);

        Log.d("PacketAnalyzer", "Protocol: " + protocol + ", Src Port: " + srcPort + ", Dst Port: " + dstPort);

        // Basic Vulnerability Detection
        if (dstPort == 23 || dstPort == 21) { // Telnet/FTP vulnerabilities
            sendIntrusionAlert("Insecure protocol detected (Port " + dstPort + ")");
        }
        if (protocol == 6 && (srcPort == 0 || dstPort == 0)) { // Null ports (potential scan)
            sendIntrusionAlert("Suspicious TCP packet with null port detected");
        }
    }

    private void sendIntrusionAlert(String message) {
        Intent intent = new Intent("com.network.ycyk.INTRUSION_ALERT");
        intent.putExtra("alert_message", message);
        sendBroadcast(intent);
    }
}
