package com.awds333.a2016.mafia.activities.server;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.activities.PlayActivity;
import com.awds333.a2016.mafia.dialogs.ExitDialog;
import com.awds333.a2016.mafia.dialogs.NoWifiDialog;
import com.awds333.a2016.mafia.dialogs.RolePickDialog;
import com.awds333.a2016.mafia.netclasses.PortsNumber;
import com.awds333.a2016.mafia.netclasses.SocketEngine;

import org.json.JSONArray;
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
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

public class WaitingForPlayersActivity extends Activity implements Observer {
    WifiManager wifiManager;
    ServerSocket guestSocket;
    Socket socket;
    Thread guestThread;
    WaitingForPlayersActivity context;
    boolean wait;
    RolePickDialog rolePick;
    PrintWriter out;
    BufferedReader reader;
    int peoplecount;
    String servername;
    SocketEngine engine;
    int port;
    boolean next;
    Object argj;
    String password;

    LinearLayout conteiner;
    LinearLayout.LayoutParams layoutParams;
    HashMap<Integer, String> idName;
    HashMap<Integer, Integer> idPort;

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
                Handler lisener = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1)
                            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 1);
                        else
                            finish();
                    }
                };
                ((NoWifiDialog) noWifiDialog).setListener(lisener);
                noWifiDialog.show(getFragmentManager(), "mytag");
            }
        } else startWaiting();
    }

    @Override
    protected void onDestroy() {
        if (next == false) {
            engine.stopPing();
            engine.deleteObserver(this);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
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
            });
            thread.start();
        }
        super.onDestroy();
    }

    private void startWaiting() {
        idName = new HashMap<Integer, String>();
        idPort = new HashMap<Integer, Integer>();
        idName.put(0, getIntent().getStringExtra("name"));
        password = getIntent().getStringExtra("lock");
        next = false;
        context = this;
        ((Button) findViewById(R.id.backbt)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.onBackPressed();
            }
        });
        ((Button) findViewById(R.id.start)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (peoplecount >= 4) {
                    Intent intent = new Intent(context, PlayActivity.class);
                    intent.putExtra("type",1);
                    intent.putExtra("mafia",rolePick.getMafias());
                    intent.putExtra("doctor",rolePick.isDoctor());
                    intent.putExtra("detective",rolePick.isDetective());
                    intent.putExtra("name",getIntent().getStringExtra("name"));
                    wait = false;
                    try {
                        guestSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    next = true;
                    engine.deleteObserver(context);
                    engine.killLockedChanalse();
                    engine.closeServerSockets();
                    startActivity(intent);
                    finish();
                } else Toast.makeText(context, R.string.needfor, Toast.LENGTH_LONG).show();
            }
        });
        ((Button) findViewById(R.id.roles)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if(peoplecount>=4){
                rolePick.show(getFragmentManager(), "myTag");
                // }
                // else Toast.makeText(context,R.string.needfor,Toast.LENGTH_LONG).show();
            }
        });
        rolePick = new RolePickDialog();
        rolePick.setContext(this);
        rolePick.newPlayer();
        conteiner = (LinearLayout) findViewById(R.id.conteiner);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        View view = LayoutInflater.from(context).inflate(R.layout.player_list_element, null);
        view.setId(0);
        ((TextView) view.findViewById(R.id.text)).setText(getIntent().getStringExtra("name"));
        conteiner.addView(view, layoutParams);
        port = PortsNumber.SERVER_GUEST_PORT + 1;
        engine = SocketEngine.getSocketEngine();
        engine.addObserver(this);
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
                        anser.put("lock", password != null);
                        out.println(anser.toString());
                    } else {
                        out.println(port);
                        engine.addChannel(port,password);
                        port++;
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
        Intent intent = new Intent(this, WaitingForPlayersActivity.class);
        intent.putExtra("name", getIntent().getStringExtra("name"));
        intent.putExtra("servname", getIntent().getStringExtra("servname"));
        intent.putExtra("lock", getIntent().getStringExtra("lock"));
        startActivity(intent);
        finish();
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
        argj = arg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject object = (JSONObject) argj;
                try {
                    String type = object.getString("type");
                    if (type.equals("newChannel")) {
                        View view = LayoutInflater.from(context).inflate(R.layout.player_list_element, null);
                        ((TextView) view.findViewById(R.id.text)).setText(object.getString("name"));
                        view.setId(object.getInt("id"));
                        conteiner.addView(view, layoutParams);
                        idPort.put(object.getInt("id"), object.getInt("port"));
                        idName.put(object.getInt("id"), object.getString("name"));
                        JSONObject message = new JSONObject();
                        message.put("type", "newPlayer");
                        message.put("name", object.getString("name"));
                        message.put("id", object.getInt("id"));
                        engine.sendMessage(message.toString());
                        rolePick.newPlayer();
                        peoplecount++;
                        if(!engine.isPinging());
//                            engine.startPing();
                    } else if (type.equals("message")) {
                        JSONObject message = new JSONObject(object.getString("message"));
                        if (message.getString("type").equals("getPlayList")) {
                            JSONObject answer = new JSONObject();
                            JSONArray array = new JSONArray();
                            Iterator<Integer> ids = idName.keySet().iterator();
                            while (ids.hasNext()) {
                                JSONObject ob = new JSONObject();
                                int idi = ids.next();
                                ob.put("id", idi);
                                ob.put("name", idName.get(idi));
                                array.put(ob);
                            }
                            answer.put("PlayList", array);
                            answer.put("type", "PlayList");
                            answer.put("id", object.getInt("id"));
                            answer.put("name", idName.get(object.getInt("id")));
                            answer.put("type", "fornew");
                            engine.sendMessageById(answer.toString(), object.getInt("id"));
                        }
                    } else if (type.equals("connectionfail")) {
                        engine.sendMessage(object.toString());
                        conteiner.removeView(conteiner.findViewById(object.getInt("id")));
                        idName.remove(object.getInt("id"));
                        idPort.remove(object.getInt("id"));
                        rolePick.byePlayer();
                        peoplecount--;
                        if (peoplecount==1)
                            engine.stopPing();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DialogFragment exit = new ExitDialog();
        exit.show(getFragmentManager(), "mytag");
    }
}
