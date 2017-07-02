package com.awds333.a2016.mafia.netclasses;


import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class SocketForPlayer {

    private static SocketForPlayer player;

    private Socket socket;
    private DataInputStream reader;
    private DataOutputStream out;
    private Thread mesThread;
    private ArrayList<byte[]> messageQueue;
    private boolean active;

    private SocketForPlayer() {
    }

    public static SocketForPlayer getSocketForPlayer() {
        if (player == null)
            player = new SocketForPlayer();
        return player;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        messageQueue = new ArrayList<byte[]>();
        active = true;
        try {
            reader = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active) {
                    if (messageQueue.size() > 0) {
                        byte[] bytes = messageQueue.get(0);
                        messageQueue.remove(0);
                        try {
                            out.writeInt(bytes.length);
                            out.write(bytes);
                        } catch (IOException e) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e1) {
                                e.printStackTrace();
                            }
                        }
                    } else try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mesThread.start();
    }

    public void sendMessage(String message) {
        Log.d("awdsawds",message);
        try {
            sendByteMessage(message.getBytes("UTF-8"));
        } catch (IOException e) {
        }
    }

    public void sendByteMessage(byte[] bytes) throws IOException {
        messageQueue.add(bytes);
    }

    public String getMessage() throws IOException {
        byte data[] =getByteMessage();
        String message = new String(data,"UTF-8");
        Log.d("awdsawds",message);
        return message;
    }

    public byte[] getByteMessage() throws IOException {
        int length = reader.readInt();
        byte[] data = new byte[length];
        reader.readFully(data);
        return  data;
    }

    public void close() {
        active = false;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        player = null;
    }
}
