package com.awds333.a2016.mafia.netclasses;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;


public class SocketEngine extends Observable {
    private static SocketEngine socketEngine;
    private ArrayList<PlayerChannel> channels;
    private ArrayList<ServerSocket> serverSockets;
    private int mport, channelId, killId, msId;
    private String mmessage, mmessageId, password;
    private Thread ping;
    private boolean pinging;

    private SocketEngine() {
        channels = new ArrayList<PlayerChannel>();
        serverSockets = new ArrayList<>();
        channelId = 1;
    }

    public static SocketEngine getSocketEngine() {
        if (socketEngine == null)
            socketEngine = new SocketEngine();
        return socketEngine;
    }

    public void addChannel(int port, String pass) {
        this.mport = port;
        password = pass;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int port = mport;
                ServerSocket serverSocket = null;
                serverSockets.add(serverSocket);
                try {
                    int mport = port;
                    serverSocket = new ServerSocket(mport);
                    Socket socket = serverSocket.accept();
                    serverSocket.close();
                    Thread thread1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PlayerChannel channel = channels.get(channels.size() - 1);
                            try {
                                JSONObject passInfo = new JSONObject();
                                String pass = channel.getPassword();
                                passInfo.put("lock", pass != null);
                                channel.sendMessage(passInfo.toString());

                                if (pass != null) {
                                    String ans = channel.getMessage();
                                    while (!ans.equals(pass)) {
                                        channel.sendMessage("no");
                                        ans = channel.getMessage();
                                    }
                                    channel.sendMessage("yes");
                                }

                                String name = channel.getMessage();
                                JSONObject newInfo = new JSONObject();
                                newInfo.put("type", "newChannel");
                                newInfo.put("name", name);
                                newInfo.put("port", channel.getPort());
                                newInfo.put("id", channel.getId());
                                Log.d("awdsawds", "new id " + channel.getId());
                                channel.unlock();
                                socketEngine.setChanged();
                                notifyObservers(newInfo);
                                while (true) {
                                    String message = channel.getMessage();
                                    JSONObject object = new JSONObject();
                                    object.put("type", "message");
                                    object.put("id", channel.getId());
                                    object.put("message", message);
                                    Log.d("awdsawds", "message id " + channel.getId());
                                    Log.d("awdsawds", message);
                                    socketEngine.setChanged();
                                    notifyObservers(object);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (!channel.isLock()) {
                                JSONObject lostConnection = new JSONObject();
                                try {
                                    lostConnection.put("type", "connectionfail");
                                    lostConnection.put("id", channel.getId());
                                    Log.d("awdsawds", "di id " + channel.getId());
                                    socketEngine.setChanged();
                                    notifyObservers(lostConnection);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else killChannelById(channel.getId());
                        }
                    });
                    PlayerChannel channeltrans = new PlayerChannel(thread1, socket, channelId, mport, password);
                    channels.add(channeltrans);
                    channelId++;
                    thread1.start();
                    serverSockets.remove(serverSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void closeServerSockets() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (serverSockets.size() > 0) {
                    try {
                        if (serverSockets.get(0) != null)
                            serverSockets.get(0).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    serverSockets.remove(0);
                }
            }
        });
        thread.start();
    }

    public void killChannelById(int id) {
        killId = id;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int id = killId;
                if (channels != null)
                    for (int i = 0; i < channels.size(); i++) {
                        if (channels.get(i) != null)
                            if (channels.get(i).getId() == id) {
                                channels.get(i).close();
                                channels.remove(channels.get(i));
                                break;
                            }
                    }
            }
        });
        thread.start();
    }

    public void sendMessage(String message) {
        mmessage = message;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String mes = mmessage;
                for (PlayerChannel channel : channels) {
                    if (!channel.isLock())
                        channel.sendMessage(mes);
                }
            }
        });
        thread.start();
    }

    public void sendMessageById(String message, int id) {
        mmessageId = message;
        msId = id;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String mes = mmessageId;
                int id = msId;
                for (PlayerChannel channel : channels) {
                    if (channel.getId() == id) {
                        channel.sendMessage(mes);
                        break;
                    }
                }
            }
        });
        thread.start();
    }

    public void startPing() {
        if (ping == null) {
            ping = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (pinging)
                        for (int i = 0; i < channels.size(); i++) {
                            try {
                                if (!channels.get(i).getAddress().isReachable(500)) {
                                    killChannelById(channels.get(i).getId());
                                    Thread.sleep(1000);
                                }
                            } catch (IOException e) {
                                killChannelById(channels.get(i).getId());
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!pinging)
                                break;
                        }
                }
            });
        }
        pinging = true;
        ping.start();
    }

    public void stopPing() {
        pinging = false;
    }

    public boolean isPinging() {
        return pinging;
    }

    public void finish() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                closeServerSockets();
                for (PlayerChannel channel : channels) {
                    channel.close();
                }
                channels = null;
                socketEngine = null;
            }
        });
        thread.start();
    }
}
