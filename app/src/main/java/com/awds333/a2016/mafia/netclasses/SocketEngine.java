package com.awds333.a2016.mafia.netclasses;

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
    private int mport;
    private int channelId;
    private int killId;
    private String mmessage;

    private SocketEngine() {
        channels = new ArrayList<PlayerChannel>();
        serverSockets = new ArrayList<>();
        channelId = 0;
    }

    public SocketEngine getSocketEngine() {
        if (socketEngine == null)
            socketEngine = new SocketEngine();
        return socketEngine;
    }

    public void addChannel(int port) {
        this.mport = port;
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
                                String name = channel.getMessage();
                                JSONObject newInfo = new JSONObject();
                                newInfo.put("type","newChannel");
                                newInfo.put("name",name);
                                newInfo.put("port",channel.getPort());
                                newInfo.put("id",channel.getId());
                                socketEngine.setChanged();
                                notifyObservers(newInfo);
                                while (true) {
                                    String message = channel.getMessage();
                                    JSONObject object = new JSONObject();
                                    object.put("type","message");
                                    object.put("id",channel.getId());
                                    object.put("message",message);
                                    socketEngine.setChanged();
                                    notifyObservers(object);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JSONObject lostConnection = new JSONObject();
                            try {
                                lostConnection.put("type","connectionfail");
                                lostConnection.put("id",channel.getId());
                                socketEngine.setChanged();
                                notifyObservers(lostConnection);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    PlayerChannel channel = new PlayerChannel(thread1, socket, channelId, mport);
                    channelId++;
                    channels.add(channel);
                    thread1.start();
                    serverSockets.remove(serverSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void closeServerSockets(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (ServerSocket serverSocket: serverSockets){
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    serverSockets.remove(serverSocket);
                }
            }
        });
        thread.start();
    }

    public void killChannelById(int id){
        killId = id;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int id = killId;
                for(PlayerChannel channel: channels){
                    if(channel.getId()==id) {
                        channel.close();
                        channels.remove(channel);
                        break;
                    }
                }
            }
        });
        thread.start();
    }

    public void sendMessage(String message){
        mmessage = message;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String mes = mmessage;
                for (PlayerChannel channel : channels){
                    channel.sendMessage(mes);
                }
            }
        });
        thread.start();
    }

    public void finish(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                closeServerSockets();
                for (PlayerChannel channel : channels){
                    channel.close();
                }
                channels = null;
                socketEngine = null;
            }
        });
        thread.start();
    }
}
