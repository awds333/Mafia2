package com.awds333.a2016.mafia.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.netclasses.SocketForPlayer;


public class PasswordDialog extends DialogFragment implements View.OnClickListener {
    View dialog;
    SocketForPlayer player;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialog = inflater.inflate(R.layout.name_dialog, null);
        ((EditText)dialog.findViewById(R.id.text)).setHint(R.string.password);
        ((EditText)dialog.findViewById(R.id.text)).setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(8);
        ((EditText)dialog.findViewById(R.id.text)).setFilters(fArray);
        ((Button)dialog.findViewById(R.id.yesbt)).setOnClickListener(this);
        ((Button)dialog.findViewById(R.id.canselbt)).setOnClickListener(this);
        return dialog;
    }

    public void setChannel(SocketForPlayer player) {
        this.player = player;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.yesbt){
            player.sendMessage(((EditText)dialog.findViewById(R.id.text)).getText().toString());
        } else {
            player.close();
            dismiss();
        }
    }
}
