package com.awds333.a2016.mafia.dialogs;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.awds333.a2016.mafia.R;

public class NoWifiDialog extends DialogFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialog = inflater.inflate(R.layout.nowifi_dialog,null);
        ((Button)dialog.findViewById(R.id.okbt)).setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onClick(View v) {
        dismiss();
        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),1);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),1);
    }
}
