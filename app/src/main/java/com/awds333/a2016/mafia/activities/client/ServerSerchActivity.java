package com.awds333.a2016.mafia.activities.client;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.dialogs.ExitDialog;
import com.awds333.a2016.mafia.dialogs.NoWifiDialog;
import com.awds333.a2016.mafia.myviews.ServerListElementView;
import com.awds333.a2016.mafia.netclasses.IpPinger;
import com.awds333.a2016.mafia.netclasses.PortsNumber;
import com.awds333.a2016.mafia.netclasses.SocketForPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class ServerSerchActivity extends Activity {
    WifiManager wifiManager;
    Handler handler, adViewHandler, connectingRezult;
    IpPinger pinger;
    ProgressBar progressBar, scaning;
    Activity context;
    ArrayList<Integer> liveIp;
    ArrayList<ServerListElementView> serverElements;
    Runnable runnable;
    String ipTail, name;
    LinearLayout listView;
    LinearLayout.LayoutParams layoutParams;
    ProgressDialog dialog;
    int connectionIp;
    SocketForPlayer player;
    ConnectivityManager connManager;
    FloatingActionButton floatingButton;
    boolean next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        next = false;
        setContentView(R.layout.activity_server_serch);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!isApOn()) {
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWifi.isConnected()) {
                startScan();
            } else {
                DialogFragment noWifiDialog = new NoWifiDialog();
                Handler lisener = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 1);
                    }
                };
                ((NoWifiDialog) noWifiDialog).setListener(lisener);
                noWifiDialog.show(getFragmentManager(), "mytag");
            }
        } else startScan();
    }

    @Override
    protected void onDestroy() {
        if (pinger != null)
            pinger.stop();
        if (!next && player != null) {
            player.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent(this, ServerSerchActivity.class);
        intent.putExtra("name", getIntent().getStringExtra("name"));
        startActivity(intent);
        finish();
    }

    private void startScan() {
        ((Button)findViewById(R.id.backbt)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.onBackPressed();
            }
        });
        scaning = (ProgressBar) findViewById(R.id.progressBar3);
        floatingButton = (FloatingActionButton) findViewById(R.id.floatingButton);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (isApOn() || mWifi.isConnected()) {
                    liveIp = new ArrayList<>();
                    listView.removeAllViews();
                    int[] myIp;
                    if (isApOn()) {
                        myIp = new int[]{192, 168, 43, 1};
                    } else {
                        String s = getMyIpAddress();
                        myIp = new int[4];
                        int count = 0;
                        StringBuilder ipb = new StringBuilder();
                        for (int i = 0; i < s.length(); i++) {
                            if (s.charAt(i) == '.') {
                                myIp[count] = Integer.parseInt(ipb.toString());
                                ipb = new StringBuilder();
                                count++;
                            } else ipb.append(s.charAt(i));
                        }
                        myIp[count] = Integer.parseInt(ipb.toString());
                    }
                    ipTail = myIp[0] + "." + myIp[1] + "." + myIp[2] + ".";
                    pinger = new IpPinger(7, myIp, handler, 500);
                    floatingButton.setClickable(false);
                    floatingButton.setVisibility(View.INVISIBLE);
                    scaning.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                } else {
                    DialogFragment noWifiDialog = new NoWifiDialog();
                    Handler lisener = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 1);
                        }
                    };
                    ((NoWifiDialog) noWifiDialog).setListener(lisener);
                    noWifiDialog.show(getFragmentManager(), "mytag");
                }
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0) {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                    if (progressBar.getProgress() == 254) {
                        floatingButton.setVisibility(View.VISIBLE);
                        floatingButton.setClickable(true);
                        scaning.setVisibility(View.INVISIBLE);
                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (!isApOn() && !mWifi.isConnected()) {
                            DialogFragment noWifiDialog = new NoWifiDialog();
                            Handler lisener = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 1);
                                }
                            };
                            ((NoWifiDialog) noWifiDialog).setListener(lisener);
                            noWifiDialog.show(getFragmentManager(), "mytag");
                        }
                    }
                } else {
                    liveIp.add(msg.what);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            }
        };
        runnable = new Runnable() {
            @Override
            public void run() {
                int ip = liveIp.get(0);
                liveIp.remove(0);
                Socket socket = new Socket();
                PrintWriter out = null;
                BufferedReader reader = null;
                try {
                    socket.connect(new InetSocketAddress(ipTail + ip, PortsNumber.SERVER_GUEST_PORT), 4000);
                    while (socket.isConnected() == false) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    JSONObject outmessage = new JSONObject();
                    outmessage.put("contyme", 1);
                    out.println(outmessage.toString());
                    JSONObject serverInfo = new JSONObject(reader.readLine());
                    int people = serverInfo.getInt("peoplecount");
                    String servername = serverInfo.getString("servername");
                    Message msg = adViewHandler.obtainMessage(ip, people, 0, servername);
                    adViewHandler.obtainMessage();
                    adViewHandler.sendMessage(msg);
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
                }
            }
        };
        adViewHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ServerListElementView servEl = new ServerListElementView(context, msg.what, msg.arg1, (String) msg.obj);
                serverElements.add(servEl);
                listView.addView(servEl.getView(), layoutParams);
                servEl.setOnClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (!isApOn() && !mWifi.isConnected()) {
                            DialogFragment noWifiDialog = new NoWifiDialog();
                            Handler lisener = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 1);
                                }
                            };
                            ((NoWifiDialog) noWifiDialog).setListener(lisener);
                            noWifiDialog.show(getFragmentManager(), "mytag");
                        } else {
                            dialog = new ProgressDialog(context);
                            dialog.setMessage(context.getString(R.string.connecting));
                            dialog.setCancelable(false);
                            dialog.show();
                            connectionIp = v.getId();
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Socket socket = new Socket();
                                    PrintWriter out = null;
                                    BufferedReader reader = null;
                                    int port = -1;
                                    try {
                                        socket.connect(new InetSocketAddress(ipTail + connectionIp, PortsNumber.SERVER_GUEST_PORT), 4000);
                                        while (socket.isConnected() == false) {
                                            try {
                                                Thread.sleep(100);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                        out = new PrintWriter(new BufferedWriter(
                                                new OutputStreamWriter(socket.getOutputStream())),
                                                true);
                                        JSONObject outmessage = new JSONObject();
                                        try {
                                            outmessage.put("contyme", 2);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        out.println(outmessage.toString());
                                        String sport = reader.readLine();
                                        port = Integer.parseInt(sport);
                                        out.close();
                                        reader.close();
                                        socket.close();
                                    } catch (IOException e) {
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
                                    }
                                    if (port == -2) {
                                        connectingRezult.sendEmptyMessage(-2);
                                    } else if (port != -1) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        socket = new Socket();
                                        try {
                                            socket.connect(new InetSocketAddress(ipTail + connectionIp, port), 4000);
                                            while (socket.isConnected() == false) {
                                                try {
                                                    Thread.sleep(100);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            player = SocketForPlayer.getSocketForPlayer();
                                            player.setSocket(socket);
                                            player.sendMessage(name);
                                            connectingRezult.sendEmptyMessage(port);
                                        } catch (IOException e) {
                                            player.close();
                                            connectingRezult.sendEmptyMessage(-1);
                                        }
                                    } else {
                                        connectingRezult.sendEmptyMessage(-1);
                                    }
                                }
                            });
                            thread.start();
                        }
                    }
                });
            }
        };
        connectingRezult = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                dialog.dismiss();
                if (msg.what == -1) {
                    Toast.makeText(context, getString(R.string.cooner), Toast.LENGTH_LONG).show();
                } else if (msg.what == -2) {
                    Toast.makeText(context, getString(R.string.gamews), Toast.LENGTH_LONG).show();
                } else {
                    next = true;
                    Intent intent = new Intent(context, WaitingForGameStartActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        name = getIntent().getStringExtra("name");
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        serverElements = new ArrayList<ServerListElementView>();
        listView = (LinearLayout) findViewById(R.id.serverlist);
        context = this;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(255);
        liveIp = new ArrayList<>();
        int[] myIp;
        if (isApOn()) {
            myIp = new int[]{192, 168, 43, 1};
        } else {
            String s = getMyIpAddress();
            myIp = new int[4];
            int count = 0;
            StringBuilder ipb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '.') {
                    myIp[count] = Integer.parseInt(ipb.toString());
                    ipb = new StringBuilder();
                    count++;
                } else ipb.append(s.charAt(i));
            }
            myIp[count] = Integer.parseInt(ipb.toString());
        }
        ipTail = myIp[0] + "." + myIp[1] + "." + myIp[2] + ".";
        pinger = new IpPinger(7, myIp, handler, 500);
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

    public String getMyIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        DialogFragment exit = new ExitDialog();
        exit.show(getFragmentManager(), "mytag");
    }
}
