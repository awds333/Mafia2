package com.awds333.a2016.mafia.activities.server;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    DataOutputStream out;
    DataInputStream reader;
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
    HashMap<Integer, byte[]> idImage;

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
        idImage = new HashMap<Integer, byte[]>();
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
                    intent.putExtra("type", 1);
                    intent.putExtra("mafia", rolePick.getMafias());
                    intent.putExtra("doctor", rolePick.isDoctor());
                    intent.putExtra("detective", rolePick.isDetective());
                    intent.putExtra("name", getIntent().getStringExtra("name"));
                    Iterator<Integer> iterator = idImage.keySet().iterator();
                    while (iterator.hasNext()){
                        int id = iterator.next();
                        intent.putExtra("image"+id,idImage.get(id));
                    }
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
        if (getIntent().getBooleanExtra("image", false)) {
            AsyncTask<Integer, Bitmap, Bitmap> myImage = new AsyncTask<Integer, Bitmap, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Integer... params) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            return null;
                    }
                    SharedPreferences sPreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
                    File file = new File(sPreferences.getString("directory", null));
                    int size = (int) file.length();
                    byte imagebytes[] = new byte[size];
                    try {
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(imagebytes, 0, imagebytes.length);
                        buf.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (imagebytes != null) {
                        idImage.put(0, imagebytes);
                        Bitmap bmp = BitmapFactory.decodeByteArray(imagebytes, 0, imagebytes.length);
                        return bmp.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    View view1 = conteiner.findViewById(0);
                    if (view1 != null&& bitmap!=null) {
                        ((ImageView) view1.findViewById(R.id.image)).setImageBitmap(bitmap);
                    }
                }
            };
            myImage.execute(0);
        }
        ((TextView) view.findViewById(R.id.name)).setText(getIntent().getStringExtra("name"));
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
                    reader = new DataInputStream(socket.getInputStream());
                    out = new DataOutputStream(socket.getOutputStream());

                    JSONObject connectiontyme = new JSONObject(getLine(reader));
                    if (connectiontyme.getInt("contyme") == 1) {
                        JSONObject anser = new JSONObject();
                        anser.put("peoplecount", peoplecount);
                        anser.put("servername", servername);
                        anser.put("lock", password != null);
                        println(out, anser.toString());
                    } else {
                        println(out, port + "");
                        engine.addChannel(port, password);
                        port++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null)
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    public String getLine(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        byte[] data = new byte[length];
        stream.readFully(data);
        return new String(data, "UTF-8");
    }

    public void println(DataOutputStream stream, String message) throws IOException {
        byte bytes[] = message.getBytes("UTF-8");
        stream.writeInt(bytes.length);
        stream.write(bytes);
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
                        ((TextView) view.findViewById(R.id.name)).setText(object.getString("name"));
                        view.setId(object.getInt("id"));
                        conteiner.addView(view, layoutParams);
                        idPort.put(object.getInt("id"), object.getInt("port"));
                        idName.put(object.getInt("id"), object.getString("name"));
                        JSONObject message = new JSONObject();
                        message.put("type", "newPlayer");
                        message.put("name", object.getString("name"));
                        message.put("id", object.getInt("id"));
                        if (object.getBoolean("hasImage")) {
                            message.put("hasImage", true);
                        } else
                            message.put("hasImage", false);
                        engine.sendMessage(message.toString());
                        rolePick.newPlayer();
                        peoplecount++;
                        Log.d("awdsawds", peoplecount + "");
                        if (!engine.isPinging()) ;
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
                                ob.put("hasImage", idImage.get(idi) != null);
                                array.put(ob);
                            }
                            answer.put("PlayList", array);
                            answer.put("type", "PlayList");
                            answer.put("id", object.getInt("id"));
                            answer.put("name", idName.get(object.getInt("id")));
                            answer.put("type", "fornew");
                            engine.sendMessageById(answer.toString(), object.getInt("id"));
                            Iterator<Integer> idsimg = idImage.keySet().iterator();
                            while (idsimg.hasNext()) {
                                int id = idsimg.next();
                                engine.sendMessageById(id+"",object.getInt("id"));
                                engine.sendByteMessageById(idImage.get(id), object.getInt("id"));
                            }
                        }
                    } else if (type.equals("connectionfail")) {
                        engine.sendMessage(object.toString());
                        conteiner.removeView(conteiner.findViewById(object.getInt("id")));
                        idName.remove(object.getInt("id"));
                        idPort.remove(object.getInt("id"));
                        idImage.remove(object.getInt("id"));
                        rolePick.byePlayer();
                        peoplecount--;
                        Log.d("awdsawds", peoplecount + "");
                        if (peoplecount == 1)
                            engine.stopPing();
                    } else if (type.equals("image")) {
                        AsyncTask<Integer, Bitmap, Bitmap> imageSet = new AsyncTask<Integer, Bitmap, Bitmap>() {
                            int id;

                            @Override
                            protected Bitmap doInBackground(Integer... params) {
                                byte[] imageBytes = engine.getContentById(params[0]);
                                id = params[0];
                                idImage.put(id, imageBytes);
                                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                JSONObject object1 = new JSONObject();
                                try {
                                    object1.put("type","image");
                                    object1.put("id",id);
                                    engine.sendMessage(object1.toString(),id);
                                    engine.sendByteMessage(imageBytes,id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return bmp.copy(Bitmap.Config.ARGB_8888, true);
                            }

                            @Override
                            protected void onPostExecute(Bitmap bitmap) {
                                View view1 = conteiner.findViewById(id);
                                if (view1 != null) {
                                    ((ImageView) view1.findViewById(R.id.image)).setImageBitmap(bitmap);
                                }
                            }
                        };
                        imageSet.execute(object.getInt("id"));
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
