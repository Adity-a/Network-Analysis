package com.network.ycyk.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.network.ycyk.AESUtils;
import com.network.ycyk.R;
import com.network.ycyk.UserData;
import com.network.ycyk.adapter.Adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;

public class VaultFragment extends Fragment {
    TextView t1, t2;
    FloatingActionButton add_new;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    ArrayList<UserData> userDataList;
    Adapter adapter;
    Button importButton;
    public static final int FILE_PICKER_REQUEST_CODE = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vault, container, false);

        t1 = view.findViewById(R.id.textView4);
        t2 = view.findViewById(R.id.textView8);
        add_new = view.findViewById(R.id.add_more);
        recyclerView = view.findViewById(R.id.recycler_view);
        importButton = view.findViewById(R.id.importButton);
        userDataList = new ArrayList<>();

        loadData();
        setupRecyclerView();

        add_new.setOnClickListener(v -> showAddDataDialog());
        importButton.setOnClickListener(v -> importFun());


        return view;
    }

    private void importFun() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/json"); // Only allow JSON files
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                importBackupFile(uri);
            }
        }
    }

    private void importBackupFile(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<UserData>>() {}.getType();
            ArrayList<UserData> importedData = gson.fromJson(jsonString.toString(), type);

            if (importedData != null) {
                userDataList.clear();  // Clear existing list
                userDataList.addAll(importedData); // Add new data

                saveImportedData(); // Save imported data to SharedPreferences
                adapter.notifyDataSetChanged(); // Notify adapter of data change

                t1.setVisibility(View.GONE);
                t2.setVisibility(View.GONE);

                Toast.makeText(getContext(), "Backup imported successfully!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Error: Invalid backup file.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to import backup.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveImportedData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userDataList);
        editor.putString("courses", json);
        editor.apply();
    }


    private void showAddDataDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null);
        alert.setView(dialogView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

        EditText type_of_data = dialogView.findViewById(R.id.data_type);
        EditText user_name_field = dialogView.findViewById(R.id.user_name_box);
        EditText user_credentials = dialogView.findViewById(R.id.user_credentials_field);
        Button submit = dialogView.findViewById(R.id.submit);
        Button generate = dialogView.findViewById(R.id.generatorButton);

        submit.setOnClickListener(v -> {
            if (type_of_data.getText().toString().isEmpty() ||
                    user_name_field.getText().toString().isEmpty() ||
                    user_credentials.getText().toString().isEmpty()) {

                Toast.makeText(requireContext(), "Enter your data", Toast.LENGTH_SHORT).show();
            } else {
                    try {
                        String data_type_enter = AESUtils.encrypt(type_of_data.getText().toString());
                        String user_name_enter = AESUtils.encrypt(user_name_field.getText().toString());
                        String credentials_enter = AESUtils.encrypt(user_credentials.getText().toString());

                        userDataList.add(new UserData(data_type_enter, user_name_enter, credentials_enter));
                        adapter.notifyItemInserted(userDataList.size());
                        alertDialog.dismiss();
                        saveData();

                        t1.setVisibility(View.GONE);
                        t2.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
        generate.setOnClickListener(v -> user_credentials.setText(generateSecurePassword()));
    }
    private void setupRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new Adapter(userDataList, requireContext());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void loadData() {
        // Load data from SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        Gson gson = new Gson();
        String json = sharedPreferences.getString("courses", null);
        Type type = new TypeToken<ArrayList<UserData>>() {}.getType();
        userDataList = gson.fromJson(json, type);

        if (userDataList == null) {
            userDataList = new ArrayList<>();
        }
    }

    private void saveData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userDataList);
        String jsonData = gson.toJson(userDataList);
        editor.putString("courses", json);
        editor.apply();
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Backup");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, "backup.json");

            FileWriter writer = new FileWriter(file);
            writer.write(jsonData);
            writer.flush();
            writer.close();

            Toast.makeText(requireContext(), "Backup saved!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to save backup!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (adapter.getItemCount() != 0) {
            t1.setVisibility(View.GONE);
            t2.setVisibility(View.GONE);
        } else {
            t1.setVisibility(View.VISIBLE);
            t2.setVisibility(View.VISIBLE);
        }
    }
    private String generateSecurePassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$₹%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }

        return password.toString();
    }
}
