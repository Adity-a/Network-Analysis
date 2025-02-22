package com.network.ycyk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.network.ycyk.R;

import java.util.List;

public class DeviceAdapter extends ArrayAdapter<String> {

    public DeviceAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, R.layout.device_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, parent, false);
        }

        String device = getItem(position);
        TextView deviceTextView = convertView.findViewById(R.id.deviceTextView);
        deviceTextView.setText(device);

        return convertView;
    }
}
