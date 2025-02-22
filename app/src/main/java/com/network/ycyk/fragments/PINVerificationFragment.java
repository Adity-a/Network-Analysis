package com.network.ycyk.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.network.ycyk.AESUtils;
import com.network.ycyk.R;

public class PINVerificationFragment extends Fragment {

    private TextInputEditText userEnterPIN;
    private TextView timer;
    private Button submit;
    private int attempt = (int) Math.floor(Math.random() * (8 - 4 + 1) + 4);

    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_p_i_n_verification, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        timer = view.findViewById(R.id.acountdown);
        userEnterPIN = view.findViewById(R.id.user_enter_pin);
        submit = view.findViewById(R.id.next_button);

        submit.setOnClickListener(v -> verifyPIN());

        return view;
    }

    private void verifyPIN() {
        String encryptedPass = sharedPreferences.getString(encrypt("PIN"), null);

        String decryptedPass = "";
        try {
            decryptedPass = AESUtils.decrypt(encryptedPass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (attempt >= 1) {
            if (userEnterPIN.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "Enter PIN to proceed", Toast.LENGTH_SHORT).show();
            }
            else if (userEnterPIN.getText().toString().length() < 4 || userEnterPIN.getText().toString().length() > 6) {
                Toast.makeText(requireContext(), "PIN must be 4-6 digits", Toast.LENGTH_SHORT).show();
            }else {
                int savedPIN = Integer.parseInt(decryptedPass);
                int enteredPIN = Integer.parseInt(userEnterPIN.getText().toString().trim());

                if (savedPIN != enteredPIN) {
                    Toast.makeText(requireContext(), "Incorrect PIN entered", Toast.LENGTH_SHORT).show();
                    Toast.makeText(requireContext(), "Attempt left: " + attempt, Toast.LENGTH_SHORT).show();
                } else {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_layout_one, new VaultFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        } else {
            Toast.makeText(requireContext(), "Attempt limit exceeded", Toast.LENGTH_SHORT).show();
            lockOutTimer();
        }
        attempt--;
    }

    private void lockOutTimer() {
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                timer.setVisibility(View.VISIBLE);
                submit.setVisibility(View.GONE);
                timer.setText("Try again after: " + millisUntilFinished / 1000 + " s");
            }

            public void onFinish() {
                timer.setVisibility(View.GONE);
                submit.setVisibility(View.VISIBLE);
                attempt = (int) Math.floor(Math.random() * (6 - 4 + 1) + 4);
            }
        }.start();
    }

    private static String encrypt(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }
}