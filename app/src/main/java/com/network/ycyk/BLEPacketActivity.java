package com.network.ycyk;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.network.ycyk.fragments.BLEPacketCaptureFragment;

public class BLEPacketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nidactivity);

        loadFragment(new BLEPacketCaptureFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Hide all fragments
        for (Fragment f : fragmentManager.getFragments()) {
            fragmentTransaction.hide(f);
        }

        // Show the desired fragment if it exists, else add it
        if (fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.add(R.id.frame_layout_two, fragment, fragment.getClass().getSimpleName());
        }

        fragmentTransaction.commit();
    }
}