package com.awds333.a2016.mafia.activities.client;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.awds333.a2016.mafia.R;
import com.awds333.a2016.mafia.dialogs.NoWifiDialog;
import com.awds333.a2016.mafia.myviews.ServerListElementView;
import com.awds333.a2016.mafia.netclasses.IpPinger;
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class ServerSerchActivity extends Activity {
    WifiManager wifiManager;
    Handler handler, adViewHandler;
    IpPinger pinger;
    ProgressBar progressBar;
    Activity context;
    ArrayList<Integer> liveIp;
    ArrayList<ServerListElementView> serverElements;
    Runnable runnable;
    String ipTail;
    LinearLayout listView;
    LinearLayout.LayoutParams layoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_serch);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!isApOn()) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                startScan();
            } else {
                DialogFragment noWifiDialog = new NoWifiDialog();
                noWifiDialog.show(getFragmentManager(), "mytag");
            }
        } else startScan();
    }

    @Override
    protected void onDestroy() {
        if(pinger!= null)
            pinger.stop();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent intent = new Intent(this, ServerSerchActivity.class);
        intent.putExtra("name", getIntent().getStringExtra("name"));
        startActivity(intent);
    }

    private void startScan() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    progressBar.setProgress(progressBar.getProgress() + 1);
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
                    try {
                        outmessage.put("contyme", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    out.println(outmessage.toString());
                    try {
                        JSONObject serverInfo = new JSONObject(reader.readLine());
                        int people = serverInfo.getInt("peoplecount");
                        String servername = serverInfo.getString("servername");
                        Message msg = adViewHandler.obtainMessage(ip, people, 0, servername);
                        adViewHandler.obtainMessage();
                        adViewHandler.sendMessage(msg);
                        out.close();
                        reader.close();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
            }
        };
        adViewHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                ServerListElementView servEl = new ServerListElementView(context, msg.what, msg.arg1, (String) msg.obj);
                serverElements.add(servEl);
                listView.addView(servEl.getView(), layoutParams);
                servEl.getView().findViewById(R.id.join).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        };
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        serverElements = new ArrayList<ServerListElementView>();
        listView = (LinearLayout) findViewById(R.id.serverlist);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        liveIp = new ArrayList<>();
        context = this;
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
}
