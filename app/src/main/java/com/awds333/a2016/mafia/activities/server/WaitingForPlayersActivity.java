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
import com.awds333.a2016.mafia.netclasses.PortsNumber;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class WaitingForPlayersActivity extends Activity implements Observer {
    WifiManager wifiManager;
    ServerSocket guestSocket;
    Socket socket;
    Thread guestThread;
    Activity context;
    boolean wait;
    PrintWriter out;
    BufferedReader reader;
    int peoplecount;
    String servername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_players);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!isApOn()) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                startWaiting();
            } else {
                DialogFragment noWifiDialog = new NoWifiDialog();
                noWifiDialog.show(getFragmentManager(), "mytag");
            }
        } else startWaiting();
    }

    @Override
    protected void onDestroy() {
        wait = false;
        try {
            guestThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void startWaiting() {
        context = this;
        wait = true;
        socket = null;
        guestSocket = null;
        out = null;
        reader = null;
        peoplecount = 1;
        servername = getIntent().getStringExtra("servername");
        guestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    guestSocket = new ServerSocket(PortsNumber.SERVER_GUEST_PORT);
                    socket = guestSocket.accept();
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    JSONObject connectiontyme = new JSONObject(reader.readLine());
                    if (connectiontyme.getInt("contyme") == 1) {
                        JSONObject anser = new JSONObject();
                        anser.put("peoplecount", peoplecount);
                        anser.put("servername", servername);
                        out.println(anser.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null)
                        out.close();
                    if (reader != null)
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    if (socket != null)
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    if (guestSocket != null)
                        try {
                            guestSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
                if (wait)
                    run();
            }
        });
        guestThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent(this, ServerSerchActivity.class);
        intent.putExtra("name", getIntent().getStringExtra("name"));
        intent.putExtra("servname", getIntent().getStringExtra("servname"));
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

    @Override
    public void update(Observable o, Object arg) {

    }
}
