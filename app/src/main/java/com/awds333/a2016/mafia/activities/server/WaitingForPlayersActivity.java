package com.awds333.a2016.mafia.activities.server;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.activities.client.ServerSerchActivity;
import com.awds333.a2016.mafia.dialogs.NoWifiDialog;

import java.lang.reflect.Method;

public class WaitingForPlayersActivity extends Activity {
WifiManager wifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_players);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!isApOn()) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                startWaiting();
            } else {
                DialogFragment noWifiDialog = new NoWifiDialog();
                noWifiDialog.show(getFragmentManager(),"mytag");
            }
        } else startWaiting();
    }

    private void startWaiting(){
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent =new Intent(this,ServerSerchActivity.class);
        intent.putExtra("name",getIntent().getStringExtra("name"));
        startActivity(intent);
    }
    public boolean isApOn() {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable ignored) {
        }
        return false;
    }
}
