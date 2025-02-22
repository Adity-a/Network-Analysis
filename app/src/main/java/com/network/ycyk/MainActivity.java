package com.network.ycyk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.network.ycyk.fragments.WifiScanFragment;
import com.network.ycyk.fragments.NetworkTopologyFragment;
import com.network.ycyk.fragments.SearchDeviceNetwork;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);

        loadFragment(new WifiScanFragment());

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.id_scan_network) {
                loadFragment(new SearchDeviceNetwork());
            } else if (itemId == R.id.id_scan_wifi) {
                loadFragment(new WifiScanFragment());
            }
            return true;
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }

    /*private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }*/
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
            fragmentTransaction.add(R.id.frame_layout_one, fragment, fragment.getClass().getSimpleName());
        }

        fragmentTransaction.commit();
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet);

        LinearLayout nTopology = dialog.findViewById(R.id.layoutTopology);
        LinearLayout pManage = dialog.findViewById(R.id.layoutPasswordManager);
        LinearLayout nidS = dialog.findViewById(R.id.layoutNIDS);
        ImageView cancelButton = dialog.findViewById(R.id.cancel_button);

        nTopology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                loadFragment(new NetworkTopologyFragment());
                //Toast.makeText(MainActivity.this, "Setting Opened !!!", Toast.LENGTH_SHORT).show();
            }
        });

        pManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, PasswordManager.class));
                finish();
            }
        });

        nidS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, BLEPacketActivity.class));
                finish();
                //Toast.makeText(MainActivity.this, "Hide Your MAC is clicked !!!", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    public void onBackPressed() {
        // Check if the current fragment is performing a scan
        NetworkTopologyFragment currentFragment = (NetworkTopologyFragment) getSupportFragmentManager()
                .findFragmentByTag(NetworkTopologyFragment.class.getSimpleName());
        SearchDeviceNetwork currentFragment1 = (SearchDeviceNetwork) getSupportFragmentManager()
                .findFragmentByTag(SearchDeviceNetwork.class.getSimpleName());

        if (currentFragment != null && currentFragment.isScanning) {
            // Do nothing if the scan is in progress
            Toast.makeText(this, "Scan in progress. Cannot go back!", Toast.LENGTH_SHORT).show();
        } else if (currentFragment1 != null && currentFragment1.scanning) {
            // Do nothing if the scan is in progress
            Toast.makeText(this, "Scan in progress. Cannot go back!", Toast.LENGTH_SHORT).show();
        } else {
            // Allow normal back press if no scanning
            super.onBackPressed();
        }
    }

}
