package com.awds333.a2016.mafia.netclasses;

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
    int channelId;

    private SocketEngine(){
        channels = new ArrayList<PlayerChannel>();
        serverSockets = new ArrayList<>();
        channelId = 0;
    }
    public SocketEngine getSocketEngine(){
        if(socketEngine == null)
            socketEngine = new SocketEngine();
        return socketEngine;
    }
    public void addChannel(int port){
        this.mport = port;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int port = mport;
                ServerSocket serverSocket = null;
                serverSockets.add(serverSocket);
                try {
                    serverSocket = new ServerSocket(port);
                    Socket socket = serverSocket.accept();
                    serverSocket.close();
                    Thread thread1 = new Thread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    PlayerChannel channel = new PlayerChannel(thread1,socket,channelId);
                    channelId++;
                    channels.add(channel);
                    thread1.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
