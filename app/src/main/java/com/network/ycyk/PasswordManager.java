package com.network.ycyk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.network.ycyk.fragments.FingerprintAuth;
import com.network.ycyk.fragments.NetworkTopologyFragment;
import com.network.ycyk.fragments.PINVerificationFragment;
import com.network.ycyk.fragments.SearchDeviceNetwork;
import com.network.ycyk.fragments.SetPINVerificationFragment;

public class PasswordManager extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_manager);

        loadFragment(new FingerprintAuth());

        ImageView backButton = findViewById(R.id.cancel);

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(PasswordManager.this, MainActivity.class));
            finish();
        });
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
            fragmentTransaction.add(R.id.frame_layout_one, fragment, fragment.getClass().getSimpleName());
        }

        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        // Check the current fragment
        FingerprintAuth currentFragment = (FingerprintAuth) getSupportFragmentManager()
                .findFragmentByTag(FingerprintAuth.class.getSimpleName());
        PINVerificationFragment currentFragment1 = (PINVerificationFragment) getSupportFragmentManager()
                .findFragmentByTag(PINVerificationFragment.class.getSimpleName());
        SetPINVerificationFragment currentFragment2 = (SetPINVerificationFragment) getSupportFragmentManager()
                .findFragmentByTag(SetPINVerificationFragment.class.getSimpleName());

        if (currentFragment != null) {
            // Do nothing
        } else if (currentFragment1 != null) {
            // Do nothing if the scan is in progress
            Toast.makeText(this, "Press Cancel", Toast.LENGTH_SHORT).show();
        } else if (currentFragment2 != null) {
            // Do nothing if the scan is in progress
            Toast.makeText(this, "Press Cancel", Toast.LENGTH_SHORT).show();
        } else {
            // Allow normal back press if no scanning
            super.onBackPressed();
            startActivity(new Intent(PasswordManager.this, MainActivity.class));
            finish();
        }
    }
}
