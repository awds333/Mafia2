package com.awds333.a2016.mafia.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awds333.a2016.mafia.R;


public class ImagePickDialog extends DialogFragment implements View.OnClickListener {
    private View view;
    private Context context;
    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.image_pick_dialog, null);
        context = getActivity();
        view.findViewById(R.id.create).setOnClickListener(this);
        view.findViewById(R.id.select).setOnClickListener(this);
        view.findViewById(R.id.remove).setOnClickListener(this);
        view.findViewById(R.id.cansel).setOnClickListener(this);
        return view;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onClick(View v) {
        if (handler == null)
            dismiss();
        else {
            if (v.getId() == R.id.create) {
                handler.sendEmptyMessage(1);
            } else if (v.getId() == R.id.select) {
                handler.sendEmptyMessage(2);
            } else if (v.getId() == R.id.remove) {
                handler.sendEmptyMessage(3);
            } else if (v.getId() == R.id.cansel) {
                dismiss();
            }
        }
    }
}
