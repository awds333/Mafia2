package com.awds333.a2016.mafia.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.awds333.a2016.mafia.R;



public class RolePickDialog extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    View dialog;
    int people;
    int mf;
    Activity context;
    boolean touched;
    boolean doctor;
    boolean detective;

    public RolePickDialog() {
        people = 0;
        mf = 0;
        touched = false;
        doctor = false;
        detective = false;
    }

    public void setContext(Activity activity){
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dialog = inflater.inflate(R.layout.role_pick_dialog, null);
        ((ImageButton) dialog.findViewById(R.id.mbye)).setOnClickListener(this);
        ((ImageButton) dialog.findViewById(R.id.mplus)).setOnClickListener(this);
        ((Button)dialog.findViewById(R.id.backbt)).setOnClickListener(this);
        ((Button)dialog.findViewById(R.id.defaultbt)).setOnClickListener(this);
        ((TextView) dialog.findViewById(R.id.mcount)).setText(mf + "");
        ((TextView) dialog.findViewById(R.id.incount)).setText((people-mf) + "");
        ((CheckBox)dialog.findViewById(R.id.detective)).setOnCheckedChangeListener(this);
        ((CheckBox)dialog.findViewById(R.id.doctor)).setOnCheckedChangeListener(this);
        ((CheckBox)dialog.findViewById(R.id.detective)).setChecked(detective);
        ((CheckBox) dialog.findViewById(R.id.doctor)).setChecked(doctor);
        return dialog;
    }

    public void newPlayer() {
        people++;
        if (mf < getBalance())
            mf++;
        if(people>=4&&!touched) {
            detective = true;
            if (people >= 6) {
                doctor = true;
            }
        }
        if(dialog!=null){
            ((TextView) dialog.findViewById(R.id.mcount)).setText(mf + "");
            ((TextView) dialog.findViewById(R.id.incount)).setText((people-mf) + "");
            ((CheckBox)dialog.findViewById(R.id.detective)).setChecked(detective);
            ((CheckBox) dialog.findViewById(R.id.doctor)).setChecked(doctor);
        }
    }

    public void byePlayer() {
        people--;
        if (mf > getBalance())
            mf--;
        if(people<6&&!touched) {
            detective = false;
            if (people < 4) {
                doctor = false;
            }
        }
        if(dialog!=null){
            ((TextView) dialog.findViewById(R.id.mcount)).setText(mf + "");
            ((TextView) dialog.findViewById(R.id.incount)).setText((people-mf) + "");
            ((CheckBox)dialog.findViewById(R.id.detective)).setChecked(detective);
            ((CheckBox) dialog.findViewById(R.id.doctor)).setChecked(doctor);
        }
    }

    private int getBalance(){
        int mbalance = 0;
        if (people >= 4 && people <= 5)
            mbalance = 1;
        else if (people >= 6 && people <= 9)
            mbalance = 2;
        else if (people >= 10 && people <= 12)
            mbalance = 3;
        else if (people >= 13)
            mbalance = (int) (people / 3.5);
        return mbalance;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.backbt){
            dismiss();
        } else if (v.getId()==R.id.defaultbt){
            touched=false;
            mf= getBalance();
            ((TextView) dialog.findViewById(R.id.mcount)).setText(mf + "");
            ((TextView) dialog.findViewById(R.id.incount)).setText((people-mf) + "");
            if(people<6&&!touched) {
                detective = false;
                if (people < 4) {
                    doctor = false;
                } else
                    doctor = true;
            } else {
                detective = true;
                doctor = true;
            }
            ((CheckBox)dialog.findViewById(R.id.detective)).setChecked(detective);
            ((CheckBox) dialog.findViewById(R.id.doctor)).setChecked(doctor);
        } else if (v.getId()==R.id.mbye){
            if(mf>1) {
                mf--;
                ((TextView) dialog.findViewById(R.id.mcount)).setText(mf + "");
                ((TextView) dialog.findViewById(R.id.incount)).setText((people-mf) + "");
            } else
                Toast.makeText(context,R.string.mfshude,Toast.LENGTH_LONG).show();
        } else if (v.getId()==R.id.mplus){
            if(people-2*mf>2) {
                mf++;
                ((TextView) dialog.findViewById(R.id.mcount)).setText(mf + "");
                ((TextView) dialog.findViewById(R.id.incount)).setText((people-mf) + "");
            } else
                Toast.makeText(context,R.string.toomach,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        touched = true;
        if(buttonView.getId()==R.id.detective)
            detective = isChecked;
        if(buttonView.getId()==R.id.doctor)
            doctor = isChecked;
    }
}
