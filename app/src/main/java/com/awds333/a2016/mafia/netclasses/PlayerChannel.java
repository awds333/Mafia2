package com.awds333.a2016.mafia.netclasses;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class PlayerChannel {
    private Thread thread,mesThread;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream reader;
    private int id;
    private int port;
    private String password;
    private boolean lock, active;
    private ArrayList<byte[]> messageQueue;

    public String getPassword() {
        return password;
    }

    public void unlock(){
        lock = false;
    }

    public boolean isLock(){
        return lock;
    }

    public PlayerChannel(Thread thread, final Socket socket, int id, int port, String pass) {
        this.thread = thread;
        this.socket = socket;
        this.id = id;
        this.port = port;
        lock = true;
        active = true;
        password = pass;
        messageQueue = new ArrayList<byte[]>();

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
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getPort() {
        return port;
    }
}
