package com.awds333.a2016.mafia.netclasses;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;


public class SocketEngine extends Observable {
    private static SocketEngine socketEngine;
    private ArrayList<PlayerChannel> channels;
    private ArrayList<ServerSocket> serverSockets;
    private int mport, channelId, killId;
    private String mmessage, password;
    private boolean pinging;
    private HashMap<Integer, byte[]> content;

    private SocketEngine() {
        channels = new ArrayList<PlayerChannel>();
        serverSockets = new ArrayList<>();
        content = new HashMap<Integer, byte[]>();
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

                                JSONObject newInfo = new JSONObject(channel.getMessage());
                                newInfo.put("type", "newChannel");
                                newInfo.put("port", channel.getPort());
                                newInfo.put("id", channel.getId());
                                channel.unlock();
                                socketEngine.setChanged();
                                notifyObservers(newInfo);
                                if (newInfo.getBoolean("hasImage")) {
                                    byte image[] = channel.getByteMessage();
                                    content.put(channel.getId(), image);
                                    JSONObject imJs = new JSONObject();
                                    imJs.put("type", "image");
                                    imJs.put("id", channel.getId());
                                    socketEngine.setChanged();
                                    notifyObservers(imJs);
                                }
                                while (true) {
                                    String message = channel.getMessage();
                                    JSONObject object = new JSONObject();
                                    object.put("type", "message");
                                    object.put("id", channel.getId());
                                    object.put("message", message);
                                    Log.d("awdsawds", object.toString());
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
                                    if (socketEngine != null) {
                                        socketEngine.setChanged();
                                        notifyObservers(lostConnection);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.d("awdsawds", channel.getId() + " died");
                            killChannelById(channel.getId());
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

    public byte[] getContentById(int id) {
        byte answer[] = content.get(id);
        content.remove(id);
        return answer;
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
                                channels.remove(i);
                                break;
                            }
                    }
            }
        });
        thread.start();
    }

    public void killLockedChanalse() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (channels != null)
                    for (int i = 0; i < channels.size(); i++) {
                        if (channels.get(i) != null)
                            if (channels.get(i).isLock()) {
                                channels.get(i).close();
                                channels.remove(channels.get(i));
                            }
                    }
            }
        });
        thread.start();
    }

    public void sendMessage(String message) {
        for (PlayerChannel channel : channels) {
            if (!channel.isLock())
                channel.sendMessage(message);
        }
    }

    public void sendMessage(String message, int pasid) {
        for (PlayerChannel channel : channels) {
            if (!channel.isLock() && channel.getId() != pasid)
                channel.sendMessage(message);
        }
    }

    public void sendByteMessage(byte message[]) {
        for (PlayerChannel channel : channels) {
            if (!channel.isLock())
                try {
                    channel.sendByteMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void sendByteMessage(byte message[], int pasid) {
        for (PlayerChannel channel : channels) {
            if (!channel.isLock() && channel.getId() != pasid)
                try {
                    channel.sendByteMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void sendMessageById(String message, int id) {
        Log.d("awdsawds", message + "  to id: " + id);
        for (PlayerChannel channel : channels) {
            if (channel.getId() == id) {
                channel.sendMessage(message);
                break;
            }
        }
    }

    public void sendByteMessageById(byte message[], int id) {
        for (PlayerChannel channel : channels) {
            if (channel.getId() == id) {
                try {
                    channel.sendByteMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }


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
                int size = channels.size();
                for (int i = 0; i < size; i++) {
                    channels.get(0).close();
                    channels.remove(0);
                }
                channels = null;
                socketEngine = null;
            }
        });
        thread.start();
    }
}
