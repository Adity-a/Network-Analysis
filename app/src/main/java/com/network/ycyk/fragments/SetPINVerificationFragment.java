package com.network.ycyk.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.network.ycyk.AESUtils;
import com.network.ycyk.R;

public class SetPINVerificationFragment extends Fragment {

    TextInputEditText enter_pin, confirm_pin;
    Button set_pin;
    private int ePIN = 0, cPIN = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_p_i_n_verification, container, false);
        setUI(view);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        set_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enter_pin.getText().toString().isEmpty() || confirm_pin.getText().toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Enter and Confirm PIN to Proceed", Toast.LENGTH_SHORT).show();
                } else {
                    ePIN = Integer.parseInt(enter_pin.getText().toString());
                    cPIN = Integer.parseInt(confirm_pin.getText().toString());

                    if (enter_pin.getText().toString().length() < 4 || enter_pin.getText().toString().length() > 6) {
                        Toast.makeText(requireContext(), "PIN must be 4-6 digits", Toast.LENGTH_SHORT).show();
                    } else if (ePIN != cPIN) {
                        Toast.makeText(requireContext(), "PINs do not match", Toast.LENGTH_SHORT).show();
                    } else {
                        String PIN = enter_pin.getText().toString().trim();

                        try {
                            PIN = AESUtils.encrypt(PIN);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        editor.putString(encrypt("PIN"), PIN);
                        editor.apply();

                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout_one, new VaultFragment()); // Replace with your Vault fragment
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
        });

        return view;
    }

    private void setUI(View view) {
        enter_pin = view.findViewById(R.id.enter_pin);
        confirm_pin = view.findViewById(R.id.confirm_pin);
        set_pin = view.findViewById(R.id.set_button);
    }

    private static String encrypt(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }
}
