package com.awds333.a2016.mafia.activities.server;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.activities.client.ServerSerchActivity;
import com.awds333.a2016.mafia.dialogs.NoWifiDialog;
import com.awds333.a2016.mafia.netclasses.PortsNumber;
import com.awds333.a2016.mafia.netclasses.SocketEngine;

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
import java.util.HashMap;
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
    SocketEngine engine;
    int port;
    boolean next;
    LinearLayout conteiner;
    LinearLayout.LayoutParams layoutParams;
    HashMap<Integer,String> idName;
    HashMap<Integer,Integer> idPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        next = true;
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
        if(next== false) {
            engine.deleteObserver(this);
            wait = false;
            try {
                guestSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                guestThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            engine.finish();
        }
        super.onDestroy();
    }

    private void startWaiting() {
        idName = new HashMap<Integer, String>();
        idPort = new HashMap<Integer, Integer>();
        next = false;
        conteiner = (LinearLayout) findViewById(R.id.conteiner);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        port = PortsNumber.SERVER_GUEST_PORT + 1;
        engine = SocketEngine.getSocketEngine();
        engine.addObserver(this);
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
                    } else {
                        out.println(port);
                        engine.addChannel(port);
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
        JSONObject object = (JSONObject) arg;
        try {
            String type = object.getString("type");
            if(type.equals("newChannel")){
                View view = LayoutInflater.from(context).inflate(R.layout.player_list_element, null);
                ((TextView)view.findViewById(R.id.name)).setText(object.getString("name"));
                view.setId(object.getInt("id"));
                conteiner.addView(view,layoutParams);
                idPort.put(object.getInt("id"),object.getInt("port"));
                idName.put(object.getInt("id"),object.getString("name"));
                JSONObject message = new JSONObject();
                message.put("type", "newPlayer");
                message.put("name", object.getString("name"));
                message.put("id",object.getInt("id"));
                engine.sendMessage(message.toString());
            } else if(type.equals("message")){

            } else if(type.equals("connectionfail")){
                engine.killChannelById(object.getInt("id"));
                engine.sendMessage(object.toString());
                conteiner.removeView(conteiner.findViewById(object.getInt("id")));
                idName.remove(object.getInt("id"));
                idPort.remove(object.getInt("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
