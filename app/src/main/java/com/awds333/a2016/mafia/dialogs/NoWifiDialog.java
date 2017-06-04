package com.awds333.a2016.mafia.dialogs;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.awds333.a2016.mafia.R;

public class NoWifiDialog extends DialogFragment implements View.OnClickListener {

    private Handler listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialog = inflater.inflate(R.layout.nowifi_dialog, null);
        ((Button) dialog.findViewById(R.id.okbt)).setOnClickListener(this);
        ((Button) dialog.findViewById(R.id.canselbt)).setOnClickListener(this);
        return dialog;
    }

    public void setListener(Handler handler) {
        listener = handler;
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (v.getId() == R.id.okbt)
            listener.sendEmptyMessage(1);
        else
            listener.sendEmptyMessage(2);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        listener.sendEmptyMessage(1);
    }
}
